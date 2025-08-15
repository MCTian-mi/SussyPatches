package dev.tianmi.sussypatches.integration.jei;

import org.jetbrains.annotations.NotNull;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.common.items.ToolItems;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
@GregTechModule(moduleID = SusModules.JEI_ID,
                containerID = Tags.MODID,
                modDependencies = Mods.Names.CONNECTED_TEXTURES_MOD,
                name = SusModules.JEI_NAME,
                description = SusModules.JEI_DESC)
public class JEIModule extends IntegrationSubmodule implements IModPlugin {

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {
        if (SusConfig.TWEAK.showAllToolItems) {
            var handler = new GTToolSubtypeHandler();
            for (var tool : ToolItems.getAllTools()) {
                subtypeRegistry.registerSubtypeInterpreter(tool.get(), handler);
            }
        }
    }
}
