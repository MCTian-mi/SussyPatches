package dev.tianmi.sussypatches.common.stoichiometry;

import gregtech.api.unification.material.Material;
import org.apache.commons.math3.optim.linear.LinearConstraint;

import java.util.*;

/**
 * Represents an independent component of the constraint system.
 * Each component maintains its own local variable indexing that maps to global indices.
 */
public class ConstraintComponent {
    final Set<CompositionVariable> variables;
    final Set<Material> unknowns;
    final Map<CompositionVariable, Integer> localVariableIndices;
    final List<LinearConstraint> constraints;
    final List<Reaction> reactions;
    int localVariableCount;

    ConstraintComponent() {
        this.variables = new HashSet<>();
        this.unknowns = new HashSet<>();

        this.localVariableIndices = new HashMap<>();
        this.constraints = new ArrayList<>();
        this.reactions = new ArrayList<>();
        this.localVariableCount = 0;
    }

    ConstraintComponent(Set<CompositionVariable> variables) {
        this.variables = new HashSet<>(variables);
        this.unknowns = new HashSet<>();

        this.localVariableIndices = new HashMap<>();
        this.constraints = new ArrayList<>();
        this.reactions = new ArrayList<>();
        this.localVariableCount = 0;

        // Initialize local indices/unknowns
        for (CompositionVariable var : variables) {
            unknowns.add(var.host);
            localVariableIndices.put(var, localVariableCount++);
        }
    }

    void addVariable(CompositionVariable var) {
        unknowns.add(var.host);
        if (!variables.contains(var)) {
            variables.add(var);
            localVariableIndices.put(var, localVariableCount++);
        }
    }


}