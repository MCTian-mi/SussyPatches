package dev.tianmi.sussypatches.core.mixin.api.pipeicontypes;

import static dev.tianmi.sussypatches.api.unification.material.info.SusIconTypes.*;

import java.util.Arrays;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.common.pipelike.cable.Insulation;

@Mixin(value = CableRenderer.class, remap = false)
public abstract class CableRendererMixin extends PipeRendererMixin {

    @Unique
    @Override
    public Iterable<MaterialIconType> sus$getPipeIconTypes() {
        return Arrays.asList(
                wire,
                insulationSingle,
                insulationDouble,
                insulationQuadruple,
                insulationOctal,
                insulationHex,
                insulationSide);
    }

    @ModifyExpressionValue(method = "buildRenderer",
                           at = @At(value = "FIELD",
                                    target = "Lgregtech/client/renderer/pipe/CableRenderer;wireTexture:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                                    opcode = Opcodes.GETFIELD))
    private TextureAtlasSprite getIconFromType(TextureAtlasSprite ignored,
                                               @Local(argsOnly = true) Material material) {
        return SusUtil.getBlockSprite(wire, material);
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "buildRenderer",
              at = @At(value = "FIELD",
                       target = "Lgregtech/client/renderer/pipe/CableRenderer;insulationTextures:[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                       opcode = Opcodes.GETFIELD,
                       args = "array=get"),
              require = 3)
    private TextureAtlasSprite getInsulationTextures(TextureAtlasSprite[] ignored, int insulationLevel,
                                                     @Local(argsOnly = true) Material material) {
        var values = Insulation.values();
        return SusUtil.getBlockSprite(SusUtil.getIconType(values[(insulationLevel + 5) % values.length]), material);
    }

    @ModifyExpressionValue(method = "getParticleTexture(Lgregtech/api/pipenet/tile/IPipeTile;)Lorg/apache/commons/lang3/tuple/Pair;",
                           at = @At(value = "FIELD",
                                    target = "Lgregtech/client/renderer/pipe/CableRenderer;wireTexture:Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                                    opcode = Opcodes.GETFIELD))
    public TextureAtlasSprite getWireSprite(TextureAtlasSprite ignored, @Local(name = "material") Material material) {
        return SusUtil.getBlockSprite(wire, material);
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "getParticleTexture(Lgregtech/api/pipenet/tile/IPipeTile;)Lorg/apache/commons/lang3/tuple/Pair;",
              at = @At(value = "FIELD",
                       target = "Lgregtech/client/renderer/pipe/CableRenderer;insulationTextures:[Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;",
                       opcode = Opcodes.GETFIELD,
                       args = "array=get"))
    public TextureAtlasSprite getInsulationSprite(TextureAtlasSprite[] ignored, int insulationLevel,
                                                  @Local(name = "material") Material material) {
        var values = Insulation.values();
        return SusUtil.getBlockSprite(SusUtil.getIconType(values[(insulationLevel + 5) % values.length]), material);
    }
}
