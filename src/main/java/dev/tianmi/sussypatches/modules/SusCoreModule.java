package dev.tianmi.sussypatches.modules;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.Tags;
import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;

@GregTechModule(moduleID = SusModules.CORE_ID,
                containerID = Tags.MODID,
                name = SusModules.CORE_NAME,
                description = SusModules.CORE_DESC,
                coreModule = true)
public class SusCoreModule implements IGregTechModule {

    @Override
    public @NotNull Logger getLogger() {
        return SussyPatches.LOGGER;
    }
}
