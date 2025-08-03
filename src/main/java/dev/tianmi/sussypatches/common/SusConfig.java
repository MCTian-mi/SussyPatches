package dev.tianmi.sussypatches.common;

import net.minecraftforge.common.config.Config;

import dev.tianmi.sussypatches.Tags;

@Config(modid = Tags.MODID)
public class SusConfig {

    @Config.Comment("Config options for additional features")
    @Config.Name("Features")
    public static final Features FEAT = new Features();

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
}
