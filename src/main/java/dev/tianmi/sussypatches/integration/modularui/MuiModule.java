package dev.tianmi.sussypatches.integration.modularui;

import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.mui.GTGuis;
import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.common.helper.GrSRecipeCreator;
import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;

@GregTechModule(moduleID = SusModules.MUI_ID,
                containerID = Tags.MODID,
                modDependencies = SusMods.Names.MODULARUI,
                name = SusModules.MUI_NAME,
                description = SusModules.MUI_DESC)
public class MuiModule extends IntegrationSubmodule {

    @Override
    @SuppressWarnings("deprecation")
    public void preInit(FMLPreInitializationEvent event) {
        if (SusConfig.API.useMui2) {
            GTGuis.registerFactories();
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (event.getSide().isClient() && SusConfig.FEAT.grsRecipeCreator) {
            GrSRecipeCreator.INSTANCE.register();
        }
    }
}
