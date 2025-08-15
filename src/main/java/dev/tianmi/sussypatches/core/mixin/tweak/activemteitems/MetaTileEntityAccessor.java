package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.api.metatileentity.MetaTileEntity;

@Mixin(value = MetaTileEntity.class, remap = false)
public interface MetaTileEntityAccessor {

    @Accessor("renderContextStack")
    ItemStack getRenderContextStack();
}
