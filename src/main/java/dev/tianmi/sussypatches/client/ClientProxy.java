package dev.tianmi.sussypatches.client;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import dev.tianmi.sussypatches.common.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void onPostInit() {
        ConnectedTextures.init();
    }
}
