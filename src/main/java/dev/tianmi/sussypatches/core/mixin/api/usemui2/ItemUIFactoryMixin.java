package dev.tianmi.sussypatches.core.mixin.api.usemui2;

import org.spongepowered.asm.mixin.Mixin;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.mui2.ItemUIFactoryExtension;
import gregtech.api.items.gui.ItemUIFactory;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2264")
@Mixin(value = ItemUIFactory.class, remap = false)
public interface ItemUIFactoryMixin extends ItemUIFactoryExtension {}
