package dev.tianmi.sussypatches.core.mixin.compat.realtimeshadercheck;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.util.OptiFineHelper;
import dev.tianmi.sussypatches.api.util.SusMods;

@Compat(mods = SusMods.OptiFine)
@Mixin(targets = "gregtech.api.util.Mods$1", remap = false)
public abstract class ModsOptiFineMixin {

    /**
     * @author Tian_mi
     * @reason Implement real-time check
     */
    @Overwrite
    public boolean isModLoaded() {
        return OptiFineHelper.isShaderActive();
    }
}
