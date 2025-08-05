package dev.tianmi.sussypatches.modules;

import dev.tianmi.sussypatches.Tags;
import gregtech.api.modules.IModuleContainer;
import gregtech.api.modules.ModuleContainer;

@ModuleContainer
public class SusModules implements IModuleContainer {

    public static final String CORE_ID = "core";
    public static final String CORE_NAME = "Sus Core";
    public static final String CORE_DESC = "Core module of SussyPatches. Not to be confused with SuSy Core.";

    public static final String ViF_ID = "vif_integration";
    public static final String ViF_NAME = "Sus ViF Integration";
    public static final String ViF_DESC = "Integration module for Vintagefix";

    @Override
    public String getID() {
        return Tags.MODID;
    }
}
