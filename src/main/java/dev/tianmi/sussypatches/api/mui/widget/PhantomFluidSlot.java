package dev.tianmi.sussypatches.api.mui.widget;

import net.minecraftforge.fluids.IFluidTank;

import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;

import gregtech.api.capability.impl.FluidTankList;

public class PhantomFluidSlot extends FluidSlot {

    @Override
    public PhantomFluidSlot syncHandler(IFluidTank fluidTank) {
        syncHandler(new FluidSlotSyncHandler(fluidTank).phantom(true));
        return this;
    }

    public FluidSlot tank(FluidTankList fluidHandler, int index) {
        return syncHandler(fluidHandler.getTankAt(index));
    }
}
