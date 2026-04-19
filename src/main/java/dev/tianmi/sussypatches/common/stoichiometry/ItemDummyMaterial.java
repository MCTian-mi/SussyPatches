package dev.tianmi.sussypatches.common.stoichiometry;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.unification.material.Material;
import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

/**
 * Dummy material for item inputs.
 */
public class ItemDummyMaterial extends Material {

    private static final Map<ItemStack, Material> stackToMaterial = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    protected ItemDummyMaterial(@NotNull ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    public static Material get(ItemStack stack) {
        if (stackToMaterial.containsKey(stack)) {
            return stackToMaterial.get(stack);
        }
        Material mat = new ItemDummyMaterial(new ResourceLocation(stack.getTranslationKey()));
        stackToMaterial.put(stack, mat);
        return mat;
    }
}
