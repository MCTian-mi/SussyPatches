package dev.tianmi.sussypatches.api.recycling;

import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.NullMarked;

/// A bare material node, i.e. an ingredient which is not tied to any specific item.
///
/// @implNote `equals`/`hashCode` are the record-generated ones, and intentionally so: [Material] itself defines
///           neither, so keying a graph on a handwritten "same name" equality would desync `equals` from
///           `hashCode` and merge materials from different registries.
@NullMarked
public record MaterialRecyclable(Material material) implements Recyclable {

    public MaterialRecyclable(UnificationEntry entry) {
        this(entry.material);
    }

    public MaterialRecyclable(MaterialStack mStack) {
        this(mStack.material);
    }

    @Override
    public int count(Object ingredient) {
        return ingredient instanceof MaterialStack mStack ? (int) mStack.amount : 1;
    }

    @Override
    public ItemStack asStack(int size) {
        throw new UnsupportedOperationException("Cannot create an ItemStack from a MaterialRecyclable instance!");
    }

    @Override
    public void addToMStack(Object2ObjectMap<Material, Fraction> mStacks, Fraction amount) {
        if (Fraction.ZERO.equals(amount)) return;
        if (material instanceof MarkerMaterial) return;
        mStacks.put(material, mStacks.getOrDefault(material, Fraction.ZERO).add(amount));
    }

    @Override
    public String toString() {
        return "M[" + material.getRegistryName() + "]";
    }
}
