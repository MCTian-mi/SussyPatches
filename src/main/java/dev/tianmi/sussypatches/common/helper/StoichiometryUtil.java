package dev.tianmi.sussypatches.common.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.tianmi.sussypatches.api.unification.SusMaterialFlags;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

/**
 * Helper methods shared by stoichiometry tooling.
 */
public final class StoichiometryUtil {

    private static final Map<Fluid, Material> FLUID_CACHE = new HashMap<>();
    private static final Map<Material, Map<Material, Long>> DECOMPOSITION_CACHE = new HashMap<>();

    private StoichiometryUtil() {}

    public static @Nullable Material getMaterialFromFluid(@Nullable FluidStack stack) {
        if (stack == null || stack.amount <= 0) {
            return null;
        }
        return getMaterialFromFluid(stack.getFluid());
    }

    public static @Nullable Material getMaterialFromFluid(@Nullable Fluid fluid) {
        if (fluid == null) {
            return null;
        }
        Material material = FLUID_CACHE.get(fluid);
        if (material != null) {
            return material;
        }

        material = FluidUnifier.getMaterialFromFluid(fluid);

        if (material != null) {
            FLUID_CACHE.put(fluid, material);
        }
        return material;
    }

    public static Map<Material, Long> flatten(ItemStack stack) {
        MaterialStack direct = OreDictUnifier.getMaterial(stack);
        if (direct != null) {
            return Map.of(direct.material, (long) direct.amount * Math.max(1, stack.getCount()));
        }
        return Map.of();
    }

    public static Map<Material, Long> flatten(Material material, long amount) {
        if (material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
            return Map.of();
        }
        Map<Material, Long> perUnit = DECOMPOSITION_CACHE.computeIfAbsent(material,
                StoichiometryUtil::computeDecomposition);
        if (perUnit.isEmpty()) {
            return perUnit;
        }
        Map<Material, Long> scaled = new HashMap<>();
        perUnit.forEach((element, value) -> scaled.put(element, value * amount));
        return scaled;
    }

    private static Map<Material, Long> computeDecomposition(Material material) {
        if (material.isElement()) {
            return Map.of(material, 1L);
        }
        Map<Material, Long> totals = new HashMap<>();
        expand(material, 1L, totals, new HashSet<>());
        return totals;
    }

    private static void expand(Material material, long multiplier, Map<Material, Long> totals,
                               Set<Material> visiting) {
        if (!visiting.add(material)) {
            return;
        }
        for (MaterialStack component : material.getMaterialComponents()) {
            Material child = component.material;
            long scaledAmount = component.amount * multiplier;
            if (child.isElement()) {
                totals.merge(child, scaledAmount, Long::sum);
                continue;
            }
            if (child.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                continue;
            }
            expand(child, scaledAmount, totals, visiting);
        }
        visiting.remove(material);
    }
}
