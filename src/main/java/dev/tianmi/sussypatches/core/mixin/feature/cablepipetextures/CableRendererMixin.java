package dev.tianmi.sussypatches.core.mixin.feature.cablepipetextures;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.uv.IconTransformation;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.common.pipelike.cable.Insulation;

@Mixin(value = CableRenderer.class, remap = false)
public abstract class CableRendererMixin {

    @Unique
    private final Map<MaterialIconSet, TextureAtlasSprite> sus$wireTextures = new HashMap<>();

    @Final
    @Shadow
    private TextureAtlasSprite[] insulationTextures;

    @Inject(method = "registerIcons",
            at = @At("TAIL"))
    private void registerSpecifiedIcon(@NotNull TextureMap textureMap, CallbackInfo ci) {
        for (MaterialIconSet iconSet : MaterialIconSet.ICON_SETS.values()) {
            sus$wireTextures.put(iconSet, textureMap.registerSprite(
                    GTUtility.gregtechId("blocks/material_sets/" + iconSet.getName().toLowerCase() + "/wire")));
        }
    }

    @Inject(method = "buildRenderer",
            at = @At("HEAD"),
            cancellable = true)
    private void rebuildCableRenderer(PipeRenderer.PipeRenderContext renderContext,
                                      BlockPipe<?, ?, ?> blockPipe,
                                      IPipeTile<?, ?> pipeTile,
                                      IPipeType<?> pipeType,
                                      @Nullable Material material,
                                      CallbackInfo ci) {
        if (material != null && pipeType instanceof Insulation cableType) {
            int insulationLevel = cableType.insulationLevel;

            MaterialIconSet iconSet = material.getMaterialIconSet();
            TextureAtlasSprite wireTexture = sus$wireTextures.getOrDefault(iconSet,
                    sus$wireTextures.get(MaterialIconSet.DULL));

            IVertexOperation wireRender = new IconTransformation(wireTexture);
            ColourMultiplier wireColor = new ColourMultiplier(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()));
            ColourMultiplier insulationColor = new ColourMultiplier(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(4210752));

            if (pipeTile != null) {
                if (pipeTile.getPaintingColor() != pipeTile.getDefaultPaintingColor()) {
                    wireColor.colour = GTUtility.convertRGBtoOpaqueRGBA_CL(pipeTile.getPaintingColor());
                }
                insulationColor.colour = GTUtility.convertRGBtoOpaqueRGBA_CL(pipeTile.getPaintingColor());
            }

            if (insulationLevel != -1) {
                if ((renderContext.getConnections() & 63) == 0) {
                    renderContext.addOpenFaceRender(false,
                            new IconTransformation(insulationTextures[5]), insulationColor);
                    return;
                }

                renderContext.addOpenFaceRender(false, wireRender, wireColor)
                        .addOpenFaceRender(false,
                                new IconTransformation(insulationTextures[insulationLevel]), insulationColor)
                        .addSideRender(false,
                                new IconTransformation(insulationTextures[5]), insulationColor);
            } else {
                renderContext.addOpenFaceRender(false, wireRender, wireColor)
                        .addSideRender(false, wireRender, wireColor);
            }

        }
        ci.cancel();
    }

    @Inject(method = "getParticleTexture(Lgregtech/api/pipenet/tile/IPipeTile;)Lorg/apache/commons/lang3/tuple/Pair;",
            at = @At("RETURN"),
            cancellable = true)
    private void setParticleTexture(IPipeTile<?, ?> pipeTile,
                                    CallbackInfoReturnable<Pair<TextureAtlasSprite, Integer>> cir) {
        if (pipeTile != null) {
            IPipeType<?> pipeType = pipeTile.getPipeType();
            if (pipeType instanceof Insulation cableType) {
                Material material;
                if (pipeTile instanceof TileEntityMaterialPipeBase<?, ?>pipeBase) {
                    material = pipeBase.getPipeMaterial();
                } else {
                    material = null;
                }

                int insulationLevel = cableType.insulationLevel;
                MaterialIconSet iconSet = material != null ? material.getMaterialIconSet() : MaterialIconSet.DULL;
                TextureAtlasSprite texture = sus$wireTextures.getOrDefault(iconSet,
                        sus$wireTextures.get(MaterialIconSet.DULL));

                int particleColor;
                if (insulationLevel == -1) {
                    particleColor = material == null ? 16777215 : material.getMaterialRGB();
                } else {
                    texture = insulationTextures[5];
                    particleColor = pipeTile.getPaintingColor();
                }
                cir.setReturnValue(Pair.of(texture, particleColor));
            }
        }
    }
}
