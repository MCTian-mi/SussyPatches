package dev.tianmi.sussypatches.api.recycling;

import dev.tianmi.sussypatches.api.util.ItemAndMeta;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingDataPublisher;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// A node of the recycling DAG: either a concrete item or a bare [Material].
///
/// @see ItemRecyclable
/// @see MaterialRecyclable
@NullMarked
public sealed interface Recyclable permits ItemRecyclable, MaterialRecyclable {

    /// Resolves an arbitrary recycling ingredient (as found in recipe definitions) to a node.
    ///
    /// Returns `null` when the input carries no recycling information, including [ItemStack#isEmpty()] stacks.
    ///
    /// @implNote Deliberately *not* accepting GroovyScript's `IIngredient` here: type-switch `Class` constants are
    ///           resolved on the first execution of this method, so any GrS type mentioned in it would turn a
    ///           boot without GroovyScript installed into a [NoClassDefFoundError]. The GrS branch lives in
    ///           `integration/grs/GrsRecyclingAdapter` instead.
    static @Nullable Recyclable from(@Nullable Object obj) {
        return switch (obj) {
            // Idempotent, so that a `Map` already keyed by [Recyclable] can be fed back in (see the GrS adapter).
            case Recyclable recyclable -> recyclable;
            case ItemStack stack -> stack.isEmpty() ? null : new ItemRecyclable(new ItemAndMeta(stack));
            case Item item -> new ItemRecyclable(new ItemAndMeta(item));
            case Block block -> new ItemRecyclable(new ItemAndMeta(new ItemStack(block)));
            case MetaValueItem metaValueItem -> new ItemRecyclable(new ItemAndMeta(metaValueItem.getStackForm()));
            case UnificationEntry entry -> new ItemRecyclable(new ItemAndMeta(OreDictUnifier.get(entry)));
            // A misspelled oreDict name resolves to an empty stack, which must not become a node: `new ItemAndMeta(EMPTY)`
            // is `minecraft:air`, i.e. a ghost predecessor contributing nothing at all.
            case String oreDict -> {
                val stack = OreDictUnifier.get(oreDict);
                yield stack.isEmpty() ? null : new ItemRecyclable(new ItemAndMeta(stack));
            }
            case MaterialStack mStack -> new MaterialRecyclable(mStack);
            case null, default -> null;
        };
    }

    /// How many units of [Material] this ingredient contributes per instance of it appearing in a recipe.
    ///
    /// @implNote Always `1` for items: the recipe's own symbol count (`RecyclingHandler#getRecyclingIngredients`)
    ///           or [gregtech.api.recipes.ingredients.GTRecipeInput#getAmount()] already carries quantity. Only
    ///           [MaterialRecyclable] needs to override this, and only for ingredients that are themselves a
    ///           [MaterialStack].
    default int count(Object ingredient) {
        return 1;
    }

    /// @throws UnsupportedOperationException if this node has no item form, e.g. [MaterialRecyclable]
    ItemStack asStack(int size);

    default ItemStack asStack() {
        return asStack(1);
    }

    /// Accumulates this node's material content into `mStacks`, weighted by `amount`.
    default void addToMStack(Object2ObjectMap<Material, Fraction> mStacks, Fraction amount) {
        if (Fraction.ZERO.equals(amount)) return;
        RecyclingDataPublisher.addItemStackToMaterialStacks(asStack(), mStacks, amount);
    }
}
