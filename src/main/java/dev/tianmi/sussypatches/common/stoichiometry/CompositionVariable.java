package dev.tianmi.sussypatches.common.stoichiometry;

import gregtech.api.unification.material.Material;

import java.util.Objects;

/**
 * Represents a variable in the LP problem: materialA_materialB
 * meaning the quantity of materialB in materialA's composition.
 */
public class CompositionVariable {
    final Material host;      // The material whose composition we're defining
    final Material component; // The component material

    CompositionVariable(Material host, Material component) {
        this.host = host;
        this.component = component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompositionVariable)) return false;
        CompositionVariable that = (CompositionVariable) o;
        return host.equals(that.host) && component.equals(that.component);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, component);
    }

    @Override
    public String toString() {
        return host + "_" + component;
    }
}
