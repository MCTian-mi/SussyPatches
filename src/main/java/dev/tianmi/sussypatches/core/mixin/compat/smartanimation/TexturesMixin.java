package dev.tianmi.sussypatches.core.mixin.compat.smartanimation;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.optifine.SmartAnimations;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.util.OptiFineHelper;
import dev.tianmi.sussypatches.api.util.SusMods;
import gregtech.client.renderer.texture.Textures;

@Compat(mods = SusMods.OptiFine)
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
        if (SmartAnimations.isActive() && sprite.hasAnimationMetadata()) {
            OptiFineHelper.setSprite(renderState.getBuffer(), sprite);
        }
    }
}
