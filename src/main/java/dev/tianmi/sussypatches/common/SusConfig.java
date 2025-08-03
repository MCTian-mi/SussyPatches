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
                "Needs CTM to be loaded",
                "Default: true"
        })
        @Config.Name("Connected Textures for Multiblocks")
        @Config.RequiresMcRestart
        public boolean CTM = true;
    }

    public static class Compat {

        @Config.Comment({
                "Fixes GregTech machines lost their animations when" +
                        "\"On-Demand Animations\" is turned on in CensoredASM",
                "Default: true"
        })
        @Config.Name("Fix On-Demand Animations")
        @Config.RequiresMcRestart
        public boolean ON_DEMAND = true;
    }
}
