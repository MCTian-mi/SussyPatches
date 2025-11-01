package dev.tianmi.sussypatches.api.item;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus;

import gregtech.api.items.metaitem.stats.IItemComponent;

/// @see dev.tianmi.sussypatches.core.mixin.feature.visiblefluidcell.MetaItemMixin
@FunctionalInterface
@ApiStatus.AvailableSince("1.9.0")
public interface IModelDispatcher extends IItemComponent {

    int getModelIndex(ItemStack itemStack);
}
