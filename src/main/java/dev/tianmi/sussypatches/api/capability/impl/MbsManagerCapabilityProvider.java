package dev.tianmi.sussypatches.api.capability.impl;

import dev.tianmi.sussypatches.api.capability.IMultiblockStateManager;
import dev.tianmi.sussypatches.api.capability.SusCapabilities;
import dev.tianmi.sussypatches.common.capability.MultiblockStateManagerImpl;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MbsManagerCapabilityProvider implements ICapabilityProvider {

    private final IMultiblockStateManager registry;

    public MbsManagerCapabilityProvider(World world) {
        this.registry = new MultiblockStateManagerImpl(world);
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == SusCapabilities.MULTIBLOCK_STATE_MANAGER;
    }

    @Nullable
    @Override
    @SuppressWarnings("ConstantValue")
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == SusCapabilities.MULTIBLOCK_STATE_MANAGER ? SusCapabilities.MULTIBLOCK_STATE_MANAGER.cast(registry) : null;
    }
}
