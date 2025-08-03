package dev.tianmi.sussypatches.core.mixin.compat.alfheim.dummyworldcrash;

import net.minecraftforge.fml.common.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;

import dev.redstudio.alfheim.lighting.LightingEngine;
import dev.tianmi.sussypatches.api.core.mixin.Implemented;
import gregtech.api.util.Mods;
import gregtech.api.util.world.DummyWorld;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2838")
@Mixin(value = DummyWorld.class, remap = false)
public class DummyWorldMixin {

    @SoftOverride
    @Optional.Method(modid = Mods.Names.ALFHEIM)
    public LightingEngine getAlfheim$lightingEngine() {
        return null;
    }
}
