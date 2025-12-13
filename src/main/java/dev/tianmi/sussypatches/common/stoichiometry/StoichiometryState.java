package dev.tianmi.sussypatches.common.stoichiometry;

import gregtech.api.unification.material.Material;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.*;

public class StoichiometryState {

    private final Set<Material> unknownMaterials;
    private final Map<CompositionVariable, ConstraintComponent> variableToComponent;
    private final List<ConstraintComponent> components;

    public StoichiometryState(Set<Material> unknownMaterials) {
        this.unknownMaterials = new HashSet<>(unknownMaterials);
        this.variableToComponent = new HashMap<>();
        this.components = new ArrayList<>();
    }

    /**
     * Adds a reaction to the verification system. This is the specific line that should
     * be highlighted when verification fails.
     *
     * @param reaction The reaction to add
     * @return true if the reaction is feasible, false if it causes infeasibility
     */
    public boolean addReaction(Reaction reaction) {
        // Step 1: Identify variables needed for this reaction
        Set<CompositionVariable> reactionVariables = getVariablesForReaction(reaction);

        if (reactionVariables.isEmpty()) {
            return true; // No unknowns involved
        }

        // Step 2: Find which existing components share ANY materials with this reaction
        // This is critical for detecting cycles through known materials
        Set<Material> reactionUnknowns = new HashSet<>();
        reactionUnknowns.addAll(reaction.getInputs().keySet());
        reactionUnknowns.addAll(reaction.getOutputs().keySet());
        reactionUnknowns.retainAll(unknownMaterials);

        Set<ConstraintComponent> affectedComponents = new HashSet<>();
        for (CompositionVariable var : reactionVariables) {
            ConstraintComponent component = variableToComponent.get(var);
            if (component != null) {
                affectedComponents.add(component);
            }
        }

        // Also check if any existing components share unknowns (not just variables)
        // This catches cycles like B -> X, X -> Z, Z -> 2B
        // TODO: merge with last check
        for (ConstraintComponent comp : new ArrayList<>(components)) {
            Set<Material> intersection = new HashSet<>(comp.unknowns);
            intersection.retainAll(reactionUnknowns);
            if (!intersection.isEmpty()) {
                affectedComponents.add(comp);
            }
        }

        // Step 3: Create or merge components as needed
        ConstraintComponent targetComponent;

        if (affectedComponents.isEmpty()) {
            // Create new component
            targetComponent = new ConstraintComponent(reactionVariables);
            components.add(targetComponent);

            // Map variables to this component
            for (CompositionVariable var : reactionVariables) {
                variableToComponent.put(var, targetComponent);
            }
        } else if (affectedComponents.size() == 1) {
            // Add to existing component
            targetComponent = affectedComponents.iterator().next();

            // Add any new variables
            for (CompositionVariable var : reactionVariables) {
                targetComponent.addVariable(var);
                variableToComponent.put(var, targetComponent);
            }
        } else {
            // Merge multiple components
            targetComponent = mergeComponents(affectedComponents, reactionVariables);
        }

        // Step 4: Generate constraints for this reaction
        List<LinearConstraint> newConstraints =
                createConstraintsForReaction(reaction, targetComponent.variables);

        targetComponent.constraints.addAll(newConstraints);
        targetComponent.reactions.add(reaction);

        // Step 5: Check feasibility of this component
        if (!isFeasible(targetComponent)) {
            return false;
        }

        return true;
    }

    /**
     * Identifies all variables needed for a reaction.
     * This includes variables connecting unknowns to ALL other materials (including knowns)
     * to ensure cycles are detected even when they pass through known materials.
     */
    private Set<CompositionVariable> getVariablesForReaction(Reaction reaction) {
        Set<CompositionVariable> variables = new HashSet<>();

        Set<Material> allMaterials = new HashSet<>();
        allMaterials.addAll(reaction.getInputs().keySet());
        allMaterials.addAll(reaction.getOutputs().keySet());

        Set<Material> unknownsInReaction = new HashSet<>();
        for (Material m : allMaterials) {
            if (unknownMaterials.contains(m)) {
                unknownsInReaction.add(m);
            }
        }

        // For each unknown, create variables for ALL other materials in the reaction
        // This includes known materials, which ensures cycles are detected
        // For example: B -> X, X -> Z, Z -> 2B forms a cycle through known material B
        for (Material unknown : unknownsInReaction) {
            for (Material other : allMaterials) {
                if (!other.equals(unknown)) {
                    variables.add(new CompositionVariable(unknown, other));
                }
            }
        }

        return variables;
    }

    /**
     * Merges multiple components that share variables.
     * This requires remapping all constraints to a new unified variable indexing.
     */
    private ConstraintComponent mergeComponents(
            Set<ConstraintComponent> componentsToMerge,
            Set<CompositionVariable> newVariables) {

        ConstraintComponent merged = new ConstraintComponent();

        // Step 1: Collect all variables and assign new local indices
        Set<CompositionVariable> allVars = new HashSet<>(newVariables);
        for (ConstraintComponent comp : componentsToMerge) {
            allVars.addAll(comp.variables);
        }

        for (CompositionVariable var : allVars) {
            merged.addVariable(var);
        }

        // Step 2: Remap constraints from each component to the new indexing
        for (ConstraintComponent comp : componentsToMerge) {
            for (LinearConstraint constraint : comp.constraints) {
                LinearConstraint remapped = remapConstraint(
                        constraint,
                        comp.localVariableIndices,
                        merged.localVariableIndices,
                        merged.localVariableCount
                );
                merged.constraints.add(remapped);
            }
            merged.reactions.addAll(comp.reactions);
        }

        // Update component mappings
        for (CompositionVariable var : merged.variables) {
            variableToComponent.put(var, merged);
        }

        // Remove old components and add merged one
        components.removeAll(componentsToMerge);
        components.add(merged);

        return merged;
    }

    /**
     * Remaps a constraint from one variable indexing to another.
     */
    private LinearConstraint remapConstraint(
            LinearConstraint constraint,
            Map<CompositionVariable, Integer> oldIndices,
            Map<CompositionVariable, Integer> newIndices,
            int newVariableCount) {

        double[] oldCoeffs = constraint.getCoefficients().toArray();
        double[] newCoeffs = new double[newVariableCount];

        // Create reverse mapping from old indices to variables
        Map<Integer, CompositionVariable> oldIndexToVar = new HashMap<>();
        for (Map.Entry<CompositionVariable, Integer> entry : oldIndices.entrySet()) {
            oldIndexToVar.put(entry.getValue(), entry.getKey());
        }

        // Remap each coefficient
        for (int oldIdx = 0; oldIdx < oldCoeffs.length; oldIdx++) {
            if (Math.abs(oldCoeffs[oldIdx]) > 1e-10) {
                CompositionVariable var = oldIndexToVar.get(oldIdx);
                if (var != null) {
                    Integer newIdx = newIndices.get(var);
                    if (newIdx != null) {
                        newCoeffs[newIdx] = oldCoeffs[oldIdx];
                    }
                }
            }
        }

        return new LinearConstraint(
                newCoeffs,
                constraint.getRelationship(),
                constraint.getValue()
        );
    }

    /**
     * Creates constraints for a single reaction using the component's local variable indexing.
     */
    private List<LinearConstraint> createConstraintsForReaction(
            Reaction reaction,
            Set<CompositionVariable> componentVariables) {

        List<LinearConstraint> constraints = new ArrayList<>();

        // Get the component to access its local indexing
        ConstraintComponent component = null;
        for (CompositionVariable var : componentVariables) {
            component = variableToComponent.get(var);
            if (component != null) break;
        }

        if (component == null) {
            return constraints;
        }

        int localVarCount = component.localVariableCount;
        Map<CompositionVariable, Integer> localIndices = component.localVariableIndices;

        // Collect all materials in the reaction
        Set<Material> allMaterials = new HashSet<>();
        allMaterials.addAll(reaction.getInputs().keySet());
        allMaterials.addAll(reaction.getOutputs().keySet());

        Set<Material> unknownsInReaction = new HashSet<>();
        for (Material m : allMaterials) {
            if (unknownMaterials.contains(m)) {
                unknownsInReaction.add(m);
            }
        }

        // Get unknowns in the input and output
        Set<Material> unknownsInInput = new HashSet<>();
        Set<Material> unknownsInOutput = new HashSet<>();
        for (Material m : reaction.getInputs().keySet()) {
            if (unknownMaterials.contains(m)) {
                unknownsInInput.add(m);
            }
        }
        for (Material m : reaction.getOutputs().keySet()) {
            if (unknownMaterials.contains(m)) {
                unknownsInOutput.add(m);
            }
        }

        // For each material that appears in the reaction
        for (Material material : allMaterials) {
            if ((unknownsInInput.contains(material) && unknownsInOutput.isEmpty()) ||
                    (unknownsInOutput.contains(material) && unknownsInInput.isEmpty())) {
                continue; // Unknowns won't be produced from anything if they're only on one side
            }

            // Calculate net change: outputs - inputs
            Fraction outputQty = reaction.getOutputs().getOrDefault(material, Fraction.ZERO);
            Fraction inputQty = reaction.getInputs().getOrDefault(material, Fraction.ZERO);

            // Build constraint for this material using local variable indexing
            double[] coefficients = new double[localVarCount];
            double constantTerm = 0;

            // Add contributions from unknowns
            for (Material unknown : unknownsInReaction) {
                if (unknown.equals(material)) {
                    continue; // Skip self-reference
                }
                if (unknownsInInput.contains(material) && unknownsInInput.contains(unknown)) {
                    continue;
                }
                if (unknownsInOutput.contains(material) && unknownsInOutput.contains(unknown)) {
                    continue;
                }

                CompositionVariable var = new CompositionVariable(unknown, material);
                Integer varIndex = localIndices.get(var);

                if (varIndex != null) {
                    Fraction unknownQtyIn = reaction.getInputs().getOrDefault(unknown, Fraction.ZERO);
                    Fraction unknownQtyOut = reaction.getOutputs().getOrDefault(unknown, Fraction.ZERO);

                    // Net coefficient: output usage - input usage
                    coefficients[varIndex] = unknownQtyOut.subtract(unknownQtyIn).doubleValue();
                }
            }

            // Constant term from known quantities
            constantTerm = outputQty.subtract(inputQty).doubleValue();

            // Check if this constraint is non-trivial
            boolean hasNonZeroCoeff = false;
            for (double coeff : coefficients) {
                if (Math.abs(coeff) > 1e-10) {
                    hasNonZeroCoeff = true;
                    break;
                }
            }

            if (!hasNonZeroCoeff && Math.abs(constantTerm) < 1e-10) {
                continue; // Trivial constraint
            }

            // Create the constraint based on reaction type
            Relationship relationship = reaction.isLossy() ?
                    Relationship.LEQ : Relationship.EQ;

            constraints.add(new LinearConstraint(coefficients, relationship, -constantTerm));
        }

        return constraints;
    }
    /**
     * Checks if a component's constraint set is feasible.
     */
    private boolean isFeasible(ConstraintComponent component) {
        if (component.constraints.isEmpty()) {
            return true;
        }

        int localVarCount = component.localVariableCount;

        // Add non-negativity constraints for all variables in this component
        List<LinearConstraint> allConstraints = new ArrayList<>(component.constraints);
        for (CompositionVariable var : component.variables) {
            Integer index = component.localVariableIndices.get(var);
            if (index != null) {
                double[] coeffs = new double[localVarCount];
                coeffs[index] = 1.0;
                allConstraints.add(new LinearConstraint(coeffs, Relationship.GEQ, 0));
            }
        }

        try {
            // Use a dummy objective function (minimize sum of component variables)
            double[] objectiveCoeffs = new double[localVarCount];
            for (CompositionVariable var : component.variables) {
                Integer index = component.localVariableIndices.get(var);
                if (index != null) {
                    objectiveCoeffs[index] = 1.0;
                }
            }

            LinearObjectiveFunction objective = new LinearObjectiveFunction(objectiveCoeffs, 0);

            SimplexSolver solver = new SimplexSolver();
            PointValuePair solution = solver.optimize(
                    objective,
                    new LinearConstraintSet(allConstraints),
                    GoalType.MINIMIZE,
                    new NonNegativeConstraint(true)
            );

            return solution != null;
        } catch (NoFeasibleSolutionException e) {
            return false;
        } catch (Exception e) {
            // Other exceptions may indicate infeasibility or unboundedness
            return false;
        }
    }

    /**
     * Returns the current state for debugging.
     */
    public VerificationState getState() {
        int totalVariables = 0;
        for (ConstraintComponent comp : components) {
            totalVariables += comp.localVariableCount;
        }

        return new VerificationState(
                components.size(),
                totalVariables,
                getTotalConstraintCount()
        );
    }

    private int getTotalConstraintCount() {
        int count = 0;
        for (ConstraintComponent comp : components) {
            count += comp.constraints.size();
        }
        return count;
    }

    /**
     * State information for debugging.
     */
    public static class VerificationState {
        private final int componentCount;
        private final int variableCount;
        private final int constraintCount;

        public VerificationState(int componentCount, int variableCount, int constraintCount) {
            this.componentCount = componentCount;
            this.variableCount = variableCount;
            this.constraintCount = constraintCount;
        }

        public int getComponentCount() {
            return componentCount;
        }

        public int getVariableCount() {
            return variableCount;
        }

        public int getConstraintCount() {
            return constraintCount;
        }

        @Override
        public String toString() {
            return String.format("Components: %d, Variables: %d, Constraints: %d",
                    componentCount, variableCount, constraintCount);
        }
    }
}