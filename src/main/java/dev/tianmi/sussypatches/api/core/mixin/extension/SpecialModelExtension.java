package dev.tianmi.sussypatches.api.core.mixin.extension;

import org.jetbrains.annotations.Nullable;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import dev.tianmi.sussypatches.api.item.IItemModelDispatcher;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;

@MixinExtension(MetaValueItem.class)
public interface SpecialModelExtension {

    static SpecialModelExtension cast(MetaValueItem metaValueItem) {
        return (SpecialModelExtension) metaValueItem;
    }

    @Nullable
    IItemModelDispatcher getItemModelDispatcher();

    @SuppressWarnings("UnusedReturnValue")
    MetaValueItem setItemModelDispatcher(IItemModelDispatcher modelDispatcher);
}
