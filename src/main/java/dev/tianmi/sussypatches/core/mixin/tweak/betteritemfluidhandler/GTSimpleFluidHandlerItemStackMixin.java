package dev.tianmi.sussypatches.core.mixin.tweak.betteritemfluidhandler;

import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import gregtech.api.capability.impl.GTSimpleFluidHandlerItemStack;

@Mixin(value = GTSimpleFluidHandlerItemStack.class, remap = false)
public abstract class GTSimpleFluidHandlerItemStackMixin extends FluidHandlerItemStackSimple {

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    GTSimpleFluidHandlerItemStackMixin() {
        super(null, 0);
    }

    /**
     * @author Tian_mi
     * @reason The whole method is unnecessary
     */
    @Overwrite
    private void removeTagWhenEmpty(boolean doDrain) {
        /* Do nothing */
    }

    @Unique
    @Override
    protected void setContainerToEmpty() {
        super.setContainerToEmpty();
        var tag = container.getTagCompound();
        if (tag != null && tag.isEmpty()) {
            container.setTagCompound(null);
        }
    }
}
