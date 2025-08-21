package dev.tianmi.sussypatches.api.util;

import net.minecraft.util.BlockRenderLayer;
import net.optifine.shaders.ShadersRender;

import gregtech.api.util.Mods;
import gregtech.client.utils.BloomEffectUtil;

public class OptiFineHelper {

    public static BlockRenderLayer getOFSafeLayer(BlockRenderLayer layer) {
        if (!Mods.Optifine.isModLoaded()) return layer;
        return layer == BloomEffectUtil.getBloomLayer() ? BloomEffectUtil.getEffectiveBloomLayer() : layer;
    }

    public static void preRenderChunkLayer(BlockRenderLayer layer) {
        if (Mods.Optifine.isModLoaded()) {
            ShadersRender.preRenderChunkLayer(getOFSafeLayer(layer));
        }
    }

    public static void postRenderChunkLayer(BlockRenderLayer layer) {
        if (Mods.Optifine.isModLoaded()) {
            ShadersRender.postRenderChunkLayer(getOFSafeLayer(layer));
        }
    }
}
