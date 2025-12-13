package dev.tianmi.sussypatches.common.stoichiometry;

import gregtech.api.unification.material.Material;
import org.apache.commons.math3.fraction.Fraction;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a reaction with materials and their quantities.
 * Supports both perfect (equality) and lossy (inequality) reactions.
 */
public class Reaction {
    private final Map<Material, Fraction> inputs;
    private final Map<Material, Fraction> outputs;
    private final boolean isLossy;
    private final String name; // For debugging

    public Reaction(Map<Material, Fraction> inputs, Map<Material, Fraction> outputs,
                    boolean isLossy, String name) {
        this.inputs = new HashMap<>(inputs);
        this.outputs = new HashMap<>(outputs);
        this.isLossy = isLossy;
        this.name = name;
    }

    public Map<Material, Fraction> getInputs() {
        return inputs;
    }

    public Map<Material, Fraction> getOutputs() {
        return outputs;
    }

    public boolean isLossy() {
        return isLossy;
    }

    public String getName() {
        return name;
    }
}
