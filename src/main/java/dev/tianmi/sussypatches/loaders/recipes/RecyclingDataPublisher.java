package dev.tianmi.sussypatches.loaders.recipes;

import com.google.common.graph.ValueGraph;
import dev.tianmi.sussypatches.api.recycling.ItemRecyclable;
import dev.tianmi.sussypatches.api.recycling.Recyclable;
import dev.tianmi.sussypatches.api.recycling.RecyclingGraph;
import dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling.OreDictUnifierAccessor;
import gregtech.api.recipes.RecyclingHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/// Projects the recycling DAG into CEu's unification map: for every item node the DAG owns, the ingredient edges
/// are resolved into an [ItemMaterialInfo] and written to `OreDictUnifier`, which is what both CEu's recycling
/// recipe generation and the in game material lookups read.
///
/// The material resolution math is ported from [RecyclingHandler].
@SuppressWarnings("UnstableApiUsage")
@NullMarked
public final class RecyclingDataPublisher {

    private RecyclingDataPublisher() {
    }

    /// Writes the resolved material list of `node` to the unification map.
    ///
    /// @implNote Nodes the DAG does not define and nodes CEu owns (MetaPrefixItems) are skipped, so data CEu manages
    ///           itself is never overwritten. A node whose ingredients resolve to nothing gets its stale entry
    ///           dropped instead, so that no recipe is rebuilt from outdated data.
    public static void publish(Recyclable node, RecyclingGraph<Recyclable> definitions) {
        if (!(node instanceof ItemRecyclable itemRecyclable) || !definitions.isDefined(node)) return;

        ItemStack outputStack = itemRecyclable.asStack();
        OrePrefix prefix = OreDictUnifier.getPrefix(outputStack);
        if (!isDagManaged(prefix)) return; // MetaPrefixItems are handled by ceu itself

        Object2ObjectOpenHashMap<Material, Fraction> mStacks = new Object2ObjectOpenHashMap<>();

        ValueGraph<Recyclable, Fraction> graph = definitions.graph();
        graph.predecessors(node).forEach(
                // Theoretically no default value is needed. However, it has to be here for CrL compatibility, since
                // [MutableValueGraph#edgeValue] returns [Optional] in newer guava versions
                ing -> ing.addToMStack(mStacks, graph.edgeValueOrDefault(ing, node, Fraction.ZERO)));

        if (mStacks.isEmpty()) {
            remove(node);
            return;
        }

        OreDictUnifierAccessor.getUnificationInfo().put(new ItemAndMetadata(itemRecyclable.asStack()),
                new ItemMaterialInfo(
                        mStacks.entrySet().stream().map(e -> new MaterialStack(e.getKey(), e.getValue().intValue()))
                                .sorted(Comparator.comparingLong(m -> -m.amount)).toList()));
    }

    /// Drops the unification entry of `node` entirely, so that its recipes are not rebuilt from stale data.
    public static void remove(Recyclable node) {
        if (node instanceof ItemRecyclable itemRecyclable) {
            OreDictUnifierAccessor.getUnificationInfo().remove(new ItemAndMetadata(itemRecyclable.asStack()));
        }
    }

    /// @return whether the DAG owns the recycling data of an item with `prefix`, i.e. whether CEu left it alone.
    ///         This is the exact negation of the guard in [#publish], so that a node is never stripped of its
    ///         recipes without being rebuilt right after.
    public static boolean isDagManaged(@Nullable OrePrefix prefix) {
        return prefix == null || prefix.getMaterialAmount(null) <= 0;
    }

    /// @see #isDagManaged(OrePrefix)
    public static boolean isDagManaged(Recyclable node) {
        return node instanceof ItemRecyclable itemRecyclable &&
                isDagManaged(OreDictUnifier.getPrefix(itemRecyclable.asStack()));
    }

    /// Copies and modifies CEu code.
    ///
    /// @see RecyclingHandler#addItemStackToMaterialStacks(ItemStack, Object2LongMap, int)
    public static void addItemStackToMaterialStacks(ItemStack itemStack, Object2ObjectMap<Material, Fraction> receiver,
                                                    Fraction amount) {
        // First try to get ItemMaterialInfo
        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(itemStack);
        if (info != null) {
            for (MaterialStack ms : info.getMaterials()) {
                if (!(ms.material instanceof MarkerMaterial)) {
                    addMaterialStack(receiver, amount, ms);
                }
            }
            return;
        }

        // Then try to get a single Material (UnificationEntry needs this, for example)
        MaterialStack materialStack = OreDictUnifier.getMaterial(itemStack);
        if (materialStack != null && !(materialStack.material instanceof MarkerMaterial)) {
            addMaterialStack(receiver, amount, materialStack);
        }

        // Gather any secondary materials if this item has an OrePrefix
        OrePrefix prefix = OreDictUnifier.getPrefix(itemStack);
        if (prefix != null && !prefix.secondaryMaterials.isEmpty()) {
            for (MaterialStack ms : prefix.secondaryMaterials) {
                addMaterialStack(receiver, amount, ms);
            }
        }
    }

    /// Copies and modifies CEu code. Adds a [MaterialStack] to a map of `<Material, Quantity>`.
    ///
    /// @param receiver the map to add to
    /// @param amount   the number of items in the stack
    /// @param ms       the stack to add
    /// @see RecyclingHandler#addMaterialStack(Object2LongMap, int, MaterialStack)
    private static void addMaterialStack(Object2ObjectMap<Material, Fraction> receiver, Fraction amount,
                                         MaterialStack ms) {
        Fraction oldAmount = receiver.getOrDefault(ms.material, Fraction.ZERO);
        receiver.put(ms.material, oldAmount.add(amount.multiplyBy(Fraction.getFraction((int) ms.amount, 1))));
    }
}
