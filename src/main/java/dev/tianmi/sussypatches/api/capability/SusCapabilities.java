package dev.tianmi.sussypatches.api.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class SusCapabilities {
    @CapabilityInject(IMultiblockStateManager.class)
    public static final Capability<IMultiblockStateManager> MULTIBLOCK_STATE_MANAGER = null;
}
