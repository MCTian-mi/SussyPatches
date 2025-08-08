package dev.tianmi.sussypatches.integration.connectedtexturesmod;

import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;

@GregTechModule(moduleID = SusModules.CTM_ID,
                containerID = Tags.MODID,
                modDependencies = Mods.Names.CONNECTED_TEXTURES_MOD,
                name = SusModules.CTM_NAME,
                description = SusModules.CTM_DESC)
public class CTMModule extends IntegrationSubmodule {

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (SusConfig.FEAT.multiCTM && event.getSide().isClient()) {
            ConnectedTextures.init();
        }
    }
}
