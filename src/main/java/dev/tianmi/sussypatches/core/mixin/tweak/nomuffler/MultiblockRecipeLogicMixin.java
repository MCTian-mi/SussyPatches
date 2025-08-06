package dev.tianmi.sussypatches.core.mixin.tweak.nomuffler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.MultiblockRecipeLogic;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2799")
@Mixin(value = MultiblockRecipeLogic.class, remap = false)
public abstract class MultiblockRecipeLogicMixin extends AbstractRecipeLogic {

    // Dummy
    @SuppressWarnings("DataFlowIssue")
    MultiblockRecipeLogicMixin() {
        super(null, null);
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Override
    @Overwrite
    protected void completeRecipe() {
        super.completeRecipe();
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Overwrite
    protected void performMufflerOperations() {
        SussyPatches.LOGGER.error(
                "Class {} is trying to call \"performMufflerOperations()\", please report to the auther!",
                this.getClass());
        throw new UnsupportedOperationException("Muffler logic has been removed!");
    }
}
