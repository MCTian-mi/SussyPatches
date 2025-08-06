package dev.tianmi.sussypatches.api.core.mixin.extension;

import java.util.Collection;

import net.minecraft.util.math.BlockPos;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.client.renderer.scene.WorldSceneRenderer;

@MixinExtension(WorldSceneRenderer.class)
public interface WSRExtension {

    static WSRExtension cast(WorldSceneRenderer wsr) {
        return (WSRExtension) wsr;
    }

    Collection<BlockPos> sus$getRenderedBlocks();
}
