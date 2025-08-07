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

    public static final String MUI_ID = "modularui_integration";
    public static final String MUI_NAME = "Sus ModularUI Integration";
    public static final String MUI_DESC = "Integration module for ModularUI";

    public static final String CTM_ID = "ctm_integration";
    public static final String CTM_NAME = "Sus CTM Integration";
    public static final String CTM_DESC = "Integration module for ConnectedTextureMod";

    @Override
    public String getID() {
        return Tags.MODID;
    }
}
