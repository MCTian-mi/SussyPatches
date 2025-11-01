package dev.tianmi.sussypatches.api.item;

import net.minecraft.item.ItemStack;

import gregtech.api.items.metaitem.stats.IItemComponent;

/// @see dev.tianmi.sussypatches.core.mixin.feature.visiblefluidcell.MetaItemMixin
@FunctionalInterface
public interface IModelDispatcher extends IItemComponent {

    int getModelIndex(ItemStack itemStack);
}
