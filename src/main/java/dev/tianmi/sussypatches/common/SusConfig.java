package dev.tianmi.sussypatches.common;

import dev.tianmi.sussypatches.Tags;
import net.minecraftforge.common.config.Config;

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
