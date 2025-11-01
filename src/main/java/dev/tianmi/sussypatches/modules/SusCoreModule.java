package dev.tianmi.sussypatches.modules;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.common.helper.ChunkAwareHook;
import dev.tianmi.sussypatches.common.helper.FluidBarRenderer;
import dev.tianmi.sussypatches.common.helper.QChestInteractions;
import dev.tianmi.sussypatches.common.helper.VisibleFluidCell;
import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;

@GregTechModule(moduleID = SusModules.CORE_ID,
                containerID = Tags.MODID,
                name = SusModules.CORE_NAME,
                description = SusModules.CORE_DESC,
                coreModule = true)
public class SusCoreModule implements IGregTechModule {

    @NotNull
    @Override
    public Logger getLogger() {
        return SussyPatches.LOGGER;
    }

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        List<Class<?>> list = new ArrayList<>();
        if (SusConfig.BUGFIX.chunkAware) list.add(ChunkAwareHook.class);
        if (SusConfig.FEAT.interactiveStorage) list.add(QChestInteractions.class);
        if (SusConfig.FEAT.fluidContainerBar && SusConfig.API.itemOverlayEvent) list.add(FluidBarRenderer.class);
        if (SusConfig.FEAT.visibleFluidCell) list.add(VisibleFluidCell.class);
        return list;
    }
}
