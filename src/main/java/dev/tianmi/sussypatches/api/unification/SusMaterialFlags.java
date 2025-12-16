package dev.tianmi.sussypatches.api.unification;

import gregtech.api.unification.material.info.MaterialFlag;

public final class SusMaterialFlags {

    private SusMaterialFlags() {}

    /**
     * Marks materials whose composition cannot exactly be represented by stoichiometry (e.g. wastewater).
     */
    public static final MaterialFlag NON_STOICHIOMETRIC = new MaterialFlag.Builder("non_stoichiometric").build();
}
