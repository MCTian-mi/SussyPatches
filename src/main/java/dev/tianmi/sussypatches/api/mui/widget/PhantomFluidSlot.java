package dev.tianmi.sussypatches.api.mui.widget;

import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;

import gregtech.api.capability.IMultipleTankHandler;

public class PhantomFluidSlot extends FluidSlot {

    @Override
    public PhantomFluidSlot syncHandler(IFluidTank fluidTank) {
        syncHandler(new FluidSlotSyncHandler(fluidTank).phantom(true));
        return this;
    }

    public PhantomFluidSlot tank(IMultipleTankHandler fluidHandler, int index) {
        return syncHandler(fluidHandler.getTankAt(index));
    }
}
