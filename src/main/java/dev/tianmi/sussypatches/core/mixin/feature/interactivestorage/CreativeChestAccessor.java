package dev.tianmi.sussypatches.core.mixin.feature.interactivestorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeChest;

@Mixin(value = MetaTileEntityCreativeChest.class, remap = false)
public interface CreativeChestAccessor {

    @Accessor("handler")
    GTItemStackHandler getHandler();
}
