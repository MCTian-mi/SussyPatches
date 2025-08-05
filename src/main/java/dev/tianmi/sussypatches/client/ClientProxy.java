package dev.tianmi.sussypatches.client;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import dev.tianmi.sussypatches.common.CommonProxy;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.util.Mods;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void onPostInit() {
        super.onPostInit();
        if (Mods.CTM.isModLoaded() && SusConfig.FEAT.multiCTM) {
            ConnectedTextures.init();
        }
    }
}
