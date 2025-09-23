package dev.tianmi.sussypatches.api.core.mixin.extension;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.client.renderer.pipe.CableRenderer;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.client.renderer.pipe.PipeRenderer;

@MixinExtension({
        PipeRenderer.class,
        ItemPipeRenderer.class,
        FluidPipeRenderer.class,
        CableRenderer.class,
})
@FunctionalInterface
/// A workaround for [IIconRegister#registerIcons] having the same name & desc as [PipeRenderer#registerIcons].
public interface IconTypeExtension {

    static IconTypeExtension cast(PipeRenderer pipe) {
        return (IconTypeExtension) pipe;
    }

    @SideOnly(Side.CLIENT)
    void onIconRegister(TextureMap textureMap);
}
