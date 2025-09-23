package dev.tianmi.sussypatches.core.mixin.api.pipeicontypes;

import static dev.tianmi.sussypatches.api.unification.material.info.SusIconTypes.*;

import java.util.Arrays;
import java.util.EnumMap;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.common.pipelike.itempipe.ItemPipeType;

@Mixin(value = ItemPipeRenderer.class, remap = false)
public abstract class ItemPipeRendererMixin extends PipeRendererMixin {

    @Unique
    @Override
    public Iterable<MaterialIconType> sus$getPipeIconTypes() {
        return Arrays.asList(
                pipeSmall,
                pipeNormal,
                pipeLarge,
                pipeHuge,
                pipeSide);
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "buildRenderer",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object getIconFromType(EnumMap<?, ?> ignored, Object pipeType,
                                   @Local(argsOnly = true) Material material) {
        return SusUtil.getBlockSprite(SusUtil.getIconType((ItemPipeType) pipeType), material);
    }

    @ModifyExpressionValue(method = "buildRenderer",
                           at = @At(value = "FIELD",
                                    target = "Lgregtech/client/renderer/texture/Textures;PIPE_SIDE:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                                    opcode = Opcodes.GETSTATIC))
    private TextureAtlasSprite getIconFromType(TextureAtlasSprite ignored,
                                               @Local(argsOnly = true) Material material) {
        return SusUtil.getBlockSprite(pipeSide, material);
    }

    @WrapOperation(method = "getParticleTexture",
                   at = @At(value = "FIELD",
                            target = "Lgregtech/client/renderer/texture/Textures;PIPE_SIDE:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                            opcode = Opcodes.GETSTATIC))
    public TextureAtlasSprite getIconFromType(Operation<TextureAtlasSprite> insn,
                                              @Local(argsOnly = true) @Nullable Material material) {
        return material == null ? insn.call() : SusUtil.getBlockSprite(pipeSide, material);
    }
}
