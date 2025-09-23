package dev.tianmi.sussypatches.core.mixin.api.pipeicontypes;

import java.util.Collections;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.core.mixin.extension.IconTypeExtension;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.client.renderer.texture.Textures;

@Mixin(value = PipeRenderer.class, remap = false)
public abstract class PipeRendererMixin implements IconTypeExtension {

    @Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/renderer/block/model/ModelResourceLocation;)V",
            at = @At("TAIL"))
    private void putIconRegister(String name, ModelResourceLocation modelLocation, CallbackInfo ci) {
        Textures.iconRegisters.add(this::onIconRegister);
    }

    @Unique
    public Iterable<MaterialIconType> sus$getPipeIconTypes() {
        return Collections.emptySet();
    }

    @Unique
    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void onIconRegister(TextureMap textureMap) {
        for (var iconType : sus$getPipeIconTypes()) {
            for (var iconSet : MaterialIconSet.ICON_SETS.values()) {
                textureMap.registerSprite(iconType.getBlockTexturePath(iconSet));
            }
        }
    }
}
