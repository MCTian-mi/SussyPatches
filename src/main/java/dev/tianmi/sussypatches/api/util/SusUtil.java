package dev.tianmi.sussypatches.api.util;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;

public class SusUtil {

    public static String getPrefix(Material material) {
        return material.getModid().equals(GTValues.MODID) ? "" : material.getModid() + ":";
    }
}
