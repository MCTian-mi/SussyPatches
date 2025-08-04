package dev.tianmi.sussypatches.core.mixin.compat.dummyworldcrash;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;

import dev.redstudio.alfheim.lighting.LightingEngine;
import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.util.Mods;
import gregtech.api.util.world.DummyWorld;

@Compat(mod = Mods.Alfheim)
@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2838")
@Mixin(value = DummyWorld.class, remap = false)
public abstract class DummyWorldMixin {

    @SoftOverride
    public LightingEngine getAlfheim$lightingEngine() {
        return null;
    }
}
