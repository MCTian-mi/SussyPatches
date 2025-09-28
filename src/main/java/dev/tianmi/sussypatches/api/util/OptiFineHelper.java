package dev.tianmi.sussypatches.api.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;

import gregtech.client.utils.BloomEffectUtil;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import mcp.MethodsReturnNonnullByDefault;

@UtilityClass
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("JavaLangInvokeHandleSignature")
public class OptiFineHelper {

    private static final MethodHandle BUFFER_SPRITE_SETTER = initSpriteSetter();

    @SneakyThrows
    private static MethodHandle initSpriteSetter() {
        if (SusMods.OptiFine.isLoaded()) {
            return MethodHandles.lookup().findVirtual(
                    BufferBuilder.class,
                    "setSprite",
                    MethodType.methodType(void.class, TextureAtlasSprite.class));
        }
        // noinspection DataFlowIssue
        return null; // OptiFine not loaded, field should never be called
    }

    public static BlockRenderLayer getOFSafeLayer(BlockRenderLayer layer) {
        if (!SusMods.ShadersMod.isLoaded()) return layer;
        return layer == BloomEffectUtil.getBloomLayer() ? BloomEffectUtil.getEffectiveBloomLayer() : layer;
    }

    public static void preRenderChunkLayer(BlockRenderLayer layer) {
        if (SusMods.ShadersMod.isLoaded()) {
            ShadersRender.preRenderChunkLayer(getOFSafeLayer(layer));
        }
    }

    public static void postRenderChunkLayer(BlockRenderLayer layer) {
        if (SusMods.ShadersMod.isLoaded()) {
            ShadersRender.postRenderChunkLayer(getOFSafeLayer(layer));
        }
    }

    @SneakyThrows
    public static void setSprite(BufferBuilder buffer, TextureAtlasSprite sprite) {
        BUFFER_SPRITE_SETTER.invokeExact(buffer, sprite);
    }

    // Dynamically check if shaders are active
    public static boolean isShaderActive() {
        return SusMods.OptiFine.isLoaded() && Shaders.shaderPackLoaded;
    }
}
