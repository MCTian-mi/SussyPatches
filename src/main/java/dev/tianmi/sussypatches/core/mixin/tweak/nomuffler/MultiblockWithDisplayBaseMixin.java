package dev.tianmi.sussypatches.core.mixin.tweak.nomuffler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2799")
@Mixin(value = MultiblockWithDisplayBase.class, remap = false)
public abstract class MultiblockWithDisplayBaseMixin extends MultiblockControllerBase {

    // Dummy
    MultiblockWithDisplayBaseMixin() {
        super(null);
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Overwrite
    public void outputRecoveryItems() {
        SussyPatches.LOGGER.error("Class {} is trying to call \"outputRecoveryItems()\", please report to the auther!",
                this.getClass());
        throw new UnsupportedOperationException("Muffler logic has been removed!");
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Overwrite
    public void outputRecoveryItems(int parallel) {
        SussyPatches.LOGGER.error(
                "Class {} is trying to call \"outputRecoveryItems(int)\", please report to the auther!",
                this.getClass());
        throw new UnsupportedOperationException("Muffler logic has been removed!");
    }
}
