package dev.tianmi.sussypatches.core.mixin.compat.ondemandanimation;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.util.SusMods;
import gregtech.client.renderer.texture.Textures;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpriteActivator;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ondemand.ICompiledChunkExpander;
import zone.rong.loliasm.config.LoliConfig;

@Compat(mods = SusMods.LoliASM)
@Mixin(value = Textures.class, remap = false)
public abstract class TexturesMixin {

    @Inject(method = "renderFace",
            at = @At(value = "INVOKE",
                     target = "Lcodechicken/lib/render/CCRenderState;render()V"))
    private static void sendAnimatedSprites(CCRenderState renderState,
                                            Matrix4 i,
                                            IVertexOperation[] only,
                                            EnumFacing need,
                                            Cuboid6 the,
                                            TextureAtlasSprite sprite,
                                            BlockRenderLayer right,
                                            CallbackInfo now) {
        if (LoliConfig.instance.onDemandAnimatedTextures && sprite.hasAnimationMetadata()) {
            var vertexFormat = renderState.getVertexFormat();
            if (vertexFormat == DefaultVertexFormats.ITEM) {
                ((IAnimatedSpriteActivator) sprite).setActive(true);
            } else if (IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get() instanceof ICompiledChunkExpander expander) {
                expander.resolve(sprite);
            }

        }
    }
}
