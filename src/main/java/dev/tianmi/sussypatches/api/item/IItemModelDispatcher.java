package dev.tianmi.sussypatches.api.item;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;

import gregtech.api.items.metaitem.stats.IItemComponent;

/// @see dev.tianmi.sussypatches.core.mixin.feature.visiblefluidcell.MetaItemMixin
@FunctionalInterface
@ApiStatus.AvailableSince("1.9.0")
public interface IItemModelDispatcher extends IItemComponent {

    /// Get the model index for the given item stack.
    /// The index range will be checked at [MetaItem#getModelIndex(ItemStack)]
    ///
    /// @param itemStack The specific item stack.
    /// @param maxIndex The max model index, from [MetaItem.MetaValueItem#getModelAmount()]`
    /// - 1`
    /// @return The model index for the specific stack, should be ranged between `0` (inclusive) and `maxIndex`
    /// (inclusive).
    int getModelIndex(ItemStack itemStack, int maxIndex);
}
