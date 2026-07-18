package dev.tianmi.sussypatches.integration.jei;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.common.helper.CollapsibleGroups;
import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.common.items.ToolItems;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.ICollapsibleGroupRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import org.jetbrains.annotations.NotNull;

@JEIPlugin
@GregTechModule(moduleID = SusModules.JEI_ID,
                containerID = Tags.MOD_ID,
                modDependencies = Mods.Names.JUST_ENOUGH_ITEMS,
                name = SusModules.JEI_NAME,
                description = SusModules.JEI_DESC)
public class JEIModule extends IntegrationSubmodule implements IModPlugin {

    @Override
    public void registerSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {
        if (SusConfig.TWEAK.showAllToolItems) {
            var handler = new GTToolSubtypeHandler();
            for (var tool : ToolItems.getAllTools()) {
                subtypeRegistry.registerSubtypeInterpreter(tool.get(), handler);
            }
        }
    }

    @Override
    public void registerCollapsibleGroups(@NotNull ICollapsibleGroupRegistry registry) {
        CollapsibleGroups.registerGroups(registry);
    }
}
