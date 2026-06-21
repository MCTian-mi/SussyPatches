package dev.tianmi.sussypatches.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.impl.GTFluidHandlerItemStack;

/// A custom child class of [GTFluidHandlerItemStack] that respects the fluid lock and voiding mechanism
///
/// @see dev.tianmi.sussypatches.core.mixin.tweak.betteritemfluidhandler.GTFluidHandlerItemStackMixin
/// @see dev.tianmi.sussypatches.core.mixin.tweak.betteritemfluidhandler.QuantumTankMixin
public class SusFluidHandlerIS extends GTFluidHandlerItemStack {

    public static final String LOCKED_FLUID_NBT_KEY = "LockedFluid";
    public static final String IS_VOIDING_NBT_KEY = "IsVoiding";

    public SusFluidHandlerIS(@NotNull ItemStack container, int capacity) {
        super(container, capacity);
    }

    @Nullable
    protected FluidStack getLockedFluid() {
        var tag = container.getTagCompound();
        if (tag == null) return null;
        return FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(LOCKED_FLUID_NBT_KEY));
    }

    protected boolean isVoiding() {
        var tag = container.getTagCompound();
        if (tag == null) return false;
        return tag.getBoolean(IS_VOIDING_NBT_KEY);
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        if (!super.canFillFluidType(fluid)) return false;
        var lockedFluid = getLockedFluid();
        return lockedFluid == null || fluid.isFluidEqual(getLockedFluid());
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null) return 0;

        int fillAmount = super.fill(resource, doFill);
        return resource.isFluidEqual(getFluid()) && isVoiding() ? resource.amount : fillAmount;
    }
}
