package dev.tianmi.sussypatches.common;

import net.minecraftforge.common.config.Config;

import dev.tianmi.sussypatches.Tags;

@Config(modid = Tags.MODID)
public class SusConfig {

    @Config.Comment("Config options for additional features")
    @Config.Name("Features")
    public static final Features FEAT = new Features();

    @Config.Comment("Config options for external mod compat")
    @Config.Name("Compatibilities")
    public static final Compat COMPAT = new Compat();

    public static class Features {

        @Config.Comment({
                "Make ConnectedTexturesMod (CTM) work on GregTech multiblocks.",
                "Needs CTM to be loaded.",
                "Default: true"
        })
        @Config.Name("Connected textures for multiblocks")
        @Config.RequiresMcRestart
        public boolean CTM = true;
    }

    public static class Compat {

        @Config.Comment({
                "Fixes GregTech machines lost their animations when " +
                        "\"On-Demand Animations\" is turned on in CensoredASM.",
                "Default: true"
        })
        @Config.Name("Fix on-demand animations")
        @Config.RequiresMcRestart
        public boolean FIX_ON_DEMAND = true;

        @Config.Comment({
                "Fixes GregTech's DummyWorld crashs with Alfheim v1.6+, " +
                        "which causes all GT recipes to disappear in JEI.",
                "Default: true"
        })
        @Config.Name("Fix Alfheim v1.6+ removing all GT recipes from JEI")
        @Config.RequiresMcRestart
        public boolean FIX_DUMMYWORLD = true;

        @Config.Comment({
                "Fixes some GT lamps losing their inventory models when " +
                        "both VintageFix and ConnectedTexturesMod are loaded.",
                "Default: true"
        })
        @Config.Name("Fix VintageFix + CTM voiding GT lamp models")
        @Config.RequiresMcRestart
        public boolean FIX_LAMP_MODEL = true;

        @Config.Comment({
                "Fixes Fluidlogged API v2 making multiblocks with fluid blocks " +
                        "in the structure failed to render the in-world preview.",
                "Fixed in Fluidlogged API v3.",
                "Default: true"
        })
        @Config.Name("Fix Fluidlogged API v2 causing multiblock render failure")
        @Config.RequiresMcRestart
        public boolean FIX_INWORLD_PREVIEW = true;
    }
}
