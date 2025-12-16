package dev.tianmi.sussypatches.common.stoichiometry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.math3.fraction.Fraction;
import org.jetbrains.annotations.Nullable;

import dev.tianmi.sussypatches.api.unification.SusMaterialFlags;
import dev.tianmi.sussypatches.api.unification.material.properties.MolarProperty;
import gregtech.api.GTValues;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;

/**
 * Helper methods shared by stoichiometry tooling.
 */
public final class StoichiometryUtil {

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
        Material material = FluidUnifier.getMaterialFromFluid(fluid);

        if (material == null) {
            // You do have to check FluidRegistry separately.
            // The wonders of experimental API!
            if (fluid == FluidRegistry.WATER) {
                return Materials.Water;
            } else if (fluid == FluidRegistry.LAVA) {
                return Materials.Lava;
            }
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

    public static Fraction getItemsPerMole(Material material) {
        if (material.isElement() || material.getMaterialComponents().isEmpty()) {
            return Fraction.ONE;
        }
        if (material.hasProperty(MolarProperty.MOLAR)) {
            return material.getProperty(MolarProperty.MOLAR).itemToMole;
        }
        return new Fraction(getItemsPerMoleRecurse(material));
    }

    private static int getItemsPerMoleRecurse(Material material) {
        if (material.isElement() || material.getMaterialComponents().isEmpty()) {
            return 1; // Elemental items: 1 item = 1 mole
        }

        // For materials with components, count the total number of atoms
        int atomCount = 0;
        for (MaterialStack component : material.getMaterialComponents()) {
            if (component.material.isElement()) {
                atomCount += component.amount;
            } else {
                // Recursively count atoms in non-elemental components
                atomCount += component.amount * getItemsPerMoleRecurse(component.material);
            }
        }

        return Math.max(1, atomCount); // At least 1 item per mole
    }

    public static Fraction getFluidPerMole(Material material) {
        if (material.hasProperty(MolarProperty.MOLAR)) {
            return material.getProperty(MolarProperty.MOLAR).fluidToMole;
        }
        if (material.hasProperty(PropertyKey.DUST)) {
            return getItemsPerMole(material).multiply(new Fraction(GTValues.L));
        }
        return new Fraction(1000);
    }

    public static Fraction getMolesFromFluid(int amount, Material mat) {
        return new Fraction(amount).divide(getFluidPerMole(mat));
    }

    public static Fraction getMolesFromItem(Fraction amount, Material mat) {
        return amount.divide(getItemsPerMole(mat));
    }
}
