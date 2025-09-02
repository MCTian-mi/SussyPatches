package dev.tianmi.sussypatches.core.mixin.feature.cablepipetextures;

import codechicken.lib.vec.uv.IconTransformation;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = FluidPipeRenderer.class, remap = false)
public abstract class FluidPipeRendererMixin {

    @Unique
    private final Table<FluidPipeType, MaterialIconSet, TextureAtlasSprite> sus$pipeTextures = HashBasedTable.create();

    @Unique
    private final Map<MaterialIconSet, TextureAtlasSprite> sus$pipeSideTextures = new HashMap<>();

    @Inject(method = "registerIcons",
            at = @At("TAIL"))
    private void registerSpecifiedIcon(TextureMap textureMap, CallbackInfo ci) {
        for (MaterialIconSet iconSet : MaterialIconSet.ICON_SETS.values()) {
            for (FluidPipeType pipeType : FluidPipeType.VALUES) {
                sus$pipeTextures.put(pipeType, iconSet, textureMap.registerSprite(
                        GTUtility.gregtechId("blocks/material_sets/" + iconSet.getName().toLowerCase()
                                + "/pipe_" + pipeType.getName() + "_in")));
            }

            sus$pipeSideTextures.put(iconSet, textureMap.registerSprite(
                    GTUtility.gregtechId("blocks/material_sets/" + iconSet.getName().toLowerCase() + "/pipe_side")));
        }
    }

    @Inject(method = "buildRenderer",
            at = @At("HEAD"),
            cancellable = true)
    private void rebuildPipeRenderer(PipeRenderer.PipeRenderContext renderContext,
                                     BlockPipe<?, ?, ?> blockPipe,
                                     IPipeTile<?, ?> pipeTile,
                                     IPipeType<?> pipeType,
                                     @Nullable Material material,
                                     CallbackInfo ci) {
        if (material != null && pipeType instanceof FluidPipeType) {
            MaterialIconSet iconSet = material.getMaterialIconSet();
            TextureAtlasSprite pipeTexture = sus$pipeTextures.get(pipeType, iconSet);
            TextureAtlasSprite pipeSideTexture = sus$pipeSideTextures.get(iconSet);

            renderContext.addOpenFaceRender(new IconTransformation(pipeTexture))
                    .addSideRender(new IconTransformation(pipeSideTexture));
        }
        ci.cancel();
    }

    @Inject(method = "getParticleTexture",
            at = @At("RETURN"),
            cancellable = true)
    private void setParticleTexture(IPipeType<?> pipeType,
                                    @Nullable Material material,
                                    CallbackInfoReturnable<TextureAtlasSprite> cir) {
        MaterialIconSet iconSet = material == null ? MaterialIconSet.DULL : material.getMaterialIconSet();
        cir.setReturnValue(sus$pipeSideTextures.get(iconSet));
    }

}
