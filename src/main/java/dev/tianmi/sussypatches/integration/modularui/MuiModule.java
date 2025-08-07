package dev.tianmi.sussypatches.integration.modularui;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.cleanroommc.modularui.factory.GuiManager;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.metatileentity.mui2.MTEGuiFactory;
import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.common.SusConfig;
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
    public void preInit(FMLPreInitializationEvent event) {
        if (SusConfig.API.useMui2) {
            GuiManager.registerFactory(MTEGuiFactory.INSTANCE);
        }
    }
}
