package dev.tianmi.sussypatches.common.stoichiometry;

import static dev.tianmi.sussypatches.common.stoichiometry.SimplexPhaseI.*;

import java.util.*;

import org.apache.commons.lang3.math.Fraction;

import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;

public class StoichiometryState {

    /**
     * Represents a general constraint template that applies to all elements.
     * When specialized to a specific element, creates unknown_element variables.
     */
    private static class GeneralConstraint {

        final Map<Material, Fraction> coefficients;  // Only for unknowns
        final Map<Element, Fraction> elementOverrides;  // Constant term per element
        final SimplexPhaseI.ConstraintType constraintType;

        GeneralConstraint(SimplexPhaseI.ConstraintType constraintType) {
            this.coefficients = new HashMap<>();
            this.elementOverrides = new HashMap<>();
            this.constraintType = constraintType;
        }
    }

    /**
     * A group of unknowns that share general constraints.
     * When elements are initialized for the group, all general constraints
     * are specialized to those elements.
     */
    private static class UnknownGroup {

        final Set<Material> unknowns;
        final List<GeneralConstraint> generalConstraints;
        final Set<Element> initializedElements;

        UnknownGroup() {
            this.unknowns = new HashSet<>();
            this.generalConstraints = new ArrayList<>();
            this.initializedElements = new HashSet<>();
        }

        void addUnknown(Material unknown) {
            unknowns.add(unknown);
        }

        void mergeFrom(UnknownGroup other) {
            unknowns.addAll(other.unknowns);
            generalConstraints.addAll(other.generalConstraints);
            initializedElements.addAll(other.initializedElements);
        }
    }

    /**
     * Manages constraints for a single element across all unknowns.
     */
    private static class PerElementSolver {

        final Element element;
        final List<ConstraintComponent> components;
        final Map<Material, ConstraintComponent> unknownToComponent;

        PerElementSolver(Element element) {
            this.element = element;
            this.components = new ArrayList<>();
            this.unknownToComponent = new HashMap<>();
        }

        /**
         * Represents an independent component within this element's constraint system.
         */
        static class ConstraintComponent {

            final Set<Material> unknowns;
            final Map<Material, Integer> localIndices;
            final List<LinearConstraint> constraints;
            int localVariableCount;

            ConstraintComponent() {
                this.unknowns = new HashSet<>();
                this.localIndices = new HashMap<>();
                this.constraints = new ArrayList<>();
                this.localVariableCount = 0;
            }

            void addUnknown(Material unknown) {
                if (!unknowns.contains(unknown)) {
                    unknowns.add(unknown);
                    localIndices.put(unknown, localVariableCount++);
                }
            }
        }
    }

    private final Map<Element, PerElementSolver> elementSolvers;
    private final List<UnknownGroup> unknownGroups;
    private final Map<Material, UnknownGroup> unknownToGroup;

    public StoichiometryState() {
        this.elementSolvers = new HashMap<>();
        this.unknownGroups = new ArrayList<>();
        this.unknownToGroup = new HashMap<>();
    }

    /**
     * Adds a reaction to the verification system.
     *
     * @return false if a reaction has no unknowns, true if it does, and throws an error if it doesn't work.
     */
    public boolean addReaction(Map<Material, Fraction> inputs, Map<Material, Fraction> outputs,
                               boolean lossy) throws StoichiometryViolationException {
        // Step 1: Identify unknowns and elements in reaction
        Set<Material> unknownsInReaction = new HashSet<>();
        Set<Element> elementsInReaction = new HashSet<>();

        for (Material m : inputs.keySet()) {
            if (!m.isElement()) {
                unknownsInReaction.add(m);
            } else {
                elementsInReaction.add(m.getElement());
            }
        }

        for (Material m : outputs.keySet()) {
            if (!m.isElement()) {
                unknownsInReaction.add(m);
            } else {
                elementsInReaction.add(m.getElement());
            }
        }

        if (unknownsInReaction.isEmpty()) {
            return false; // No unknowns involved
        }

        // Step 2: Handle unknown grouping
        Set<UnknownGroup> affectedGroups = new HashSet<>();
        for (Material unknown : unknownsInReaction) {
            UnknownGroup group = unknownToGroup.get(unknown);
            if (group != null) {
                affectedGroups.add(group);
            }
        }

        UnknownGroup targetGroup;
        if (affectedGroups.isEmpty()) {
            targetGroup = new UnknownGroup();
            unknownGroups.add(targetGroup);
        } else if (affectedGroups.size() == 1) {
            targetGroup = affectedGroups.iterator().next();
        } else {
            targetGroup = mergeGroups(affectedGroups);
        }

        // Add unknowns to group
        for (Material unknown : unknownsInReaction) {
            targetGroup.addUnknown(unknown);
            unknownToGroup.put(unknown, targetGroup);
        }

        // Step 3: Create general constraint and element overrides
        GeneralConstraint generalConstraint = createGeneralConstraint(inputs, outputs, lossy);
        targetGroup.generalConstraints.add(generalConstraint);

        // Initialize elements that haven't been initialized yet for this group
        Set<Element> newlyInitializedElements = new HashSet<>();
        for (Element element : elementsInReaction) {
            if (!targetGroup.initializedElements.contains(element)) {
                newlyInitializedElements.add(element);
                initializeElementForGroup(element, targetGroup);
            }
        }

        // Apply this new constraint only to initialized elements
        // (Newly initialized elements already got all constraints via initializeElementForGroup)
        for (Element element : targetGroup.initializedElements) {
            if (!newlyInitializedElements.contains(element)) {
                addSpecializedConstraint(generalConstraint, element, targetGroup);
            }
        }

        return true;
    }

    /**
     * Creates a general constraint from a reaction.
     */
    private GeneralConstraint createGeneralConstraint(Map<Material, Fraction> inputs, Map<Material, Fraction> outputs,
                                                      boolean lossy) {
        GeneralConstraint gc = new GeneralConstraint(
                lossy ? ConstraintType.LEQ : ConstraintType.EQ);

        // Process all materials in the reaction
        Set<Material> allMaterials = new HashSet<>();
        allMaterials.addAll(inputs.keySet());
        allMaterials.addAll(outputs.keySet());

        for (Material material : allMaterials) {
            Fraction outputQty = outputs.getOrDefault(material, Fraction.ZERO);
            Fraction inputQty = inputs.getOrDefault(material, Fraction.ZERO);
            Fraction netChange = outputQty.subtract(inputQty);

            if (!material.isElement()) {
                // Unknown: add to coefficients
                gc.coefficients.put(material, netChange);
            } else if (material.getElement() != null) {
                // Known element: add override (negative of net change)
                gc.elementOverrides.put(material.getElement(), netChange.negate());
            }
        }

        return gc;
    }

    /**
     * Initializes an element for a group by creating variables and specializing all constraints.
     */
    private void initializeElementForGroup(Element element, UnknownGroup group) {
        group.initializedElements.add(element);

        // Get or create solver for this element
        PerElementSolver solver = elementSolvers.get(element);
        if (solver == null) {
            solver = new PerElementSolver(element);
            elementSolvers.put(element, solver);
        }

        // Specialize all existing general constraints to this element
        for (GeneralConstraint gc : group.generalConstraints) {
            addSpecializedConstraint(gc, element, group);
        }
    }

    /**
     * Specializes a general constraint to a specific element and adds it to the solver.
     * Errors if the constraint causes infeasibility.
     */
    private void addSpecializedConstraint(GeneralConstraint gc, Element element, UnknownGroup group) {
        PerElementSolver solver = elementSolvers.get(element);
        if (solver == null) {
            return;
        }

        // Determine which unknowns are involved
        Set<Material> involvedUnknowns = new HashSet<>(gc.coefficients.keySet());
        involvedUnknowns.retainAll(group.unknowns);

        if (involvedUnknowns.isEmpty()) {
            return;
        }

        // Find affected components
        Set<PerElementSolver.ConstraintComponent> affectedComponents = new HashSet<>();
        for (Material unknown : involvedUnknowns) {
            PerElementSolver.ConstraintComponent comp = solver.unknownToComponent.get(unknown);
            if (comp != null) {
                affectedComponents.add(comp);
            }
        }

        // Get or create target component
        PerElementSolver.ConstraintComponent targetComponent;
        if (affectedComponents.isEmpty()) {
            targetComponent = new PerElementSolver.ConstraintComponent();
            solver.components.add(targetComponent);
        } else if (affectedComponents.size() == 1) {
            targetComponent = affectedComponents.iterator().next();
        } else {
            targetComponent = mergeComponents(affectedComponents, solver);
        }

        // Add unknowns to component
        for (Material unknown : involvedUnknowns) {
            targetComponent.addUnknown(unknown);
            solver.unknownToComponent.put(unknown, targetComponent);
        }

        // Build the specialized constraint
        double[] coefficients = new double[targetComponent.localVariableCount];
        for (Map.Entry<Material, Fraction> entry : gc.coefficients.entrySet()) {
            Material unknown = entry.getKey();
            if (involvedUnknowns.contains(unknown)) {
                Integer index = targetComponent.localIndices.get(unknown);
                if (index != null) {
                    coefficients[index] = entry.getValue().doubleValue();
                }
            }
        }

        // Get constant term from override (or 0 if no override)
        Fraction constantTerm = gc.elementOverrides.getOrDefault(element, Fraction.ZERO);

        // Add constraint
        LinearConstraint constraint = new LinearConstraint(
                coefficients,
                gc.constraintType,
                constantTerm.doubleValue()  // Already negated and moved to RHS
        );
        targetComponent.constraints.add(constraint);

        // Check feasibility
        checkFeasibility(targetComponent, element);
    }

    /**
     * Merges unknown groups and cross-specializes constraints.
     */
    private UnknownGroup mergeGroups(Set<UnknownGroup> groupsToMerge) {
        UnknownGroup merged = new UnknownGroup();

        // Collect all groups' data
        List<UnknownGroup> groupList = new ArrayList<>(groupsToMerge);
        for (UnknownGroup group : groupList) {
            merged.mergeFrom(group);

            // Update mappings
            for (Material unknown : group.unknowns) {
                unknownToGroup.put(unknown, merged);
            }
        }

        // Cross-specialize: apply each group's general constraints to other groups' elements
        for (int i = 0; i < groupList.size(); i++) {
            UnknownGroup groupI = groupList.get(i);

            for (int j = 0; j < groupList.size(); j++) {
                if (i == j) continue;

                UnknownGroup groupJ = groupList.get(j);

                // For each element initialized in group J but not in group I
                Set<Element> elementsToSpecialize = new HashSet<>(groupJ.initializedElements);
                elementsToSpecialize.removeAll(groupI.initializedElements);

                // Specialize group I's constraints to these elements
                for (Element element : elementsToSpecialize) {
                    for (GeneralConstraint gc : groupI.generalConstraints) {
                        addSpecializedConstraint(gc, element, merged);
                    }
                }
            }
        }

        // Remove old groups and add merged one
        unknownGroups.removeAll(groupsToMerge);
        unknownGroups.add(merged);

        return merged;
    }

    /**
     * Merges components within an element solver.
     */
    private PerElementSolver.ConstraintComponent mergeComponents(
                                                                 Set<PerElementSolver.ConstraintComponent> componentsToMerge,
                                                                 PerElementSolver solver) {
        PerElementSolver.ConstraintComponent merged = new PerElementSolver.ConstraintComponent();

        // Collect all unknowns and assign new indices
        for (PerElementSolver.ConstraintComponent comp : componentsToMerge) {
            for (Material unknown : comp.unknowns) {
                merged.addUnknown(unknown);
            }
        }

        // Remap and add constraints
        for (PerElementSolver.ConstraintComponent comp : componentsToMerge) {
            for (LinearConstraint constraint : comp.constraints) {
                LinearConstraint remapped = remapConstraint(
                        constraint,
                        comp.localIndices,
                        merged.localIndices,
                        merged.localVariableCount);
                merged.constraints.add(remapped);
            }
        }

        // Update mappings
        for (Material unknown : merged.unknowns) {
            solver.unknownToComponent.put(unknown, merged);
        }

        // Remove old components and add merged one
        solver.components.removeAll(componentsToMerge);
        solver.components.add(merged);

        return merged;
    }

    /**
     * Remaps a constraint from one variable indexing to another.
     */
    private LinearConstraint remapConstraint(
                                             LinearConstraint constraint,
                                             Map<Material, Integer> oldIndices,
                                             Map<Material, Integer> newIndices,
                                             int newVariableCount) {
        double[] oldCoeffs = constraint.coefficients;
        double[] newCoeffs = new double[newVariableCount];

        // Create reverse mapping
        Map<Integer, Material> oldIndexToMaterial = new HashMap<>();
        for (Map.Entry<Material, Integer> entry : oldIndices.entrySet()) {
            oldIndexToMaterial.put(entry.getValue(), entry.getKey());
        }

        // Remap coefficients
        for (int oldIdx = 0; oldIdx < oldCoeffs.length; oldIdx++) {
            if (Math.abs(oldCoeffs[oldIdx]) > 1e-10) {
                Material material = oldIndexToMaterial.get(oldIdx);
                if (material != null) {
                    Integer newIdx = newIndices.get(material);
                    if (newIdx != null) {
                        newCoeffs[newIdx] = oldCoeffs[oldIdx];
                    }
                }
            }
        }

        return new LinearConstraint(
                newCoeffs,
                constraint.type,
                constraint.rhs);
    }

    /**
     * Checks if a component is feasible.
     */
    private void checkFeasibility(PerElementSolver.ConstraintComponent component, Element element) {
        if (component.constraints.isEmpty()) {
            return;
        }

        // Build constraint list for simplex solver
        List<LinearConstraint> constraints = new ArrayList<>();

        // Add the component's constraints
        for (LinearConstraint c : component.constraints) {
            constraints.add(new LinearConstraint(c.coefficients, c.type, c.rhs));
        }

        // Add non-negativity constraints for all variables
        for (Material unknown : component.unknowns) {
            Integer index = component.localIndices.get(unknown);
            if (index != null) {
                double[] coeffs = new double[component.localVariableCount];
                coeffs[index] = 1.0;
                constraints.add(new LinearConstraint(
                        coeffs,
                        SimplexPhaseI.ConstraintType.GEQ,
                        0.0));
            }
        }

        if (SimplexPhaseI.isFeasible(constraints, component.localVariableCount)) {
            return;
        }
        StringBuilder msg = new StringBuilder(
                "No feasible solution found for unknown materials in element " + element.getSymbol());
        msg.append("\nLocal constraints (all greater than 0):");
        // Display all linear constraints
        for (LinearConstraint constraint : constraints) {
            if (constraint.type == ConstraintType.GEQ) {
                continue; // Don't use non-negativity constraints
            }
            msg.append("\n").append(formatConstraint(constraint, element));
        }
        // Display index information in order
        msg.append("\nCoefficient labels:");
        // Reverse the index map:
        Material[] unknowns = new Material[component.localVariableCount];
        for (Map.Entry<Material, Integer> entry : component.localIndices.entrySet()) {
            unknowns[entry.getValue()] = entry.getKey();
        }
        for (int i = 0; i < component.localVariableCount; i++) {
            msg.append("\n").append((char) ('a' + i)).append(": ").append(unknowns[i]);
        }
        msg.append(
                "\nUse this information to find the other offending lines. The latest constraint will now be removed");
        constraints.remove(constraints.size() - 1);
        throw new StoichiometryViolationException(msg.toString());
    }

    private String formatConstraint(LinearConstraint constraint, Element element) {
        StringBuilder left = new StringBuilder();
        StringBuilder right = new StringBuilder();

        double[] coeffs = constraint.coefficients;
        for (int i = 0; i < coeffs.length; i++) {
            double coeff = coeffs[i];
            if (coeff > 0) {
                right.append(coeff).append((char) ('a' + i)).append(" + ");
            } else if (coeff < 0) {
                left.append(-coeff).append((char) ('a' + i)).append(" + ");
            }
        }
        if (constraint.rhs < 0) {
            right.append(-constraint.rhs).append(element.getSymbol()).append(" + ");
        } else if (constraint.rhs > 0) {
            left.append(constraint.rhs).append(element.getSymbol()).append(" + ");
        }
        // Both now have to have " + " at the end if they're not empty
        if (!right.isEmpty()) {
            right.delete(right.length() - 2, right.length());
        }
        if (!left.isEmpty()) {
            left.delete(left.length() - 2, left.length());
        }

        if (right.isEmpty()) {
            right.append("nothing ");
        }
        if (left.isEmpty()) {
            left.append("nothing ");
        }
        return left.toString() +
                constraint.type.oppositeType() + " " + // <= -> >=
                right.toString();
    }

    public void clear() {
        this.elementSolvers.clear();
        this.unknownGroups.clear();
        this.unknownToGroup.clear();
    }

    /**
     * Returns verification state for debugging.
     */
    public VerificationState getState() {
        int totalComponents = 0;
        int totalVariables = 0;
        int totalConstraints = 0;

        for (PerElementSolver solver : elementSolvers.values()) {
            totalComponents += solver.components.size();
            for (PerElementSolver.ConstraintComponent comp : solver.components) {
                totalVariables += comp.localVariableCount;
                totalConstraints += comp.constraints.size();
            }
        }

        return new VerificationState(
                unknownGroups.size(),
                elementSolvers.size(),
                totalComponents,
                totalVariables,
                totalConstraints);
    }

    /**
     * Debugging state information.
     */
    public static class VerificationState {

        private final int groupCount;
        private final int elementCount;
        private final int componentCount;
        private final int variableCount;
        private final int constraintCount;

        public VerificationState(int groupCount, int elementCount, int componentCount,
                                 int variableCount, int constraintCount) {
            this.groupCount = groupCount;
            this.elementCount = elementCount;
            this.componentCount = componentCount;
            this.variableCount = variableCount;
            this.constraintCount = constraintCount;
        }

        public int getGroupCount() {
            return groupCount;
        }

        public int getElementCount() {
            return elementCount;
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
            return String.format(
                    "Groups: %d, Elements: %d, Components: %d, Variables: %d, Constraints: %d",
                    groupCount, elementCount, componentCount, variableCount, constraintCount);
        }
    }
}
