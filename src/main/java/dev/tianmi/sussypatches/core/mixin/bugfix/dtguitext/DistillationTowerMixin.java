package dev.tianmi.sussypatches.core.mixin.bugfix.dtguitext;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDistillationTower;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2691")
@Mixin(value = MetaTileEntityDistillationTower.class, remap = false)
public abstract class DistillationTowerMixin extends RecipeMapMultiblockController {

    // Dummy
    DistillationTowerMixin() {
        super(null, null);
    }

    /// @author Tian_mi
    /// @reason The entire logic is both wrong and unnecessary
    @Override
    @Overwrite
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
    }
}
