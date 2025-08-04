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

    @Config.Comment("Config options for fixing... bugs")
    @Config.Name("Bugfixes")
    public static final Bugfix BUGFIX = new Bugfix();

    @Config.Comment("Config options for tweaking existing features")
    @Config.Name("Bugfixes")
    public static final Tweaks TWEAKS = new Tweaks();

    public static final class Features {

        @Config.Comment({
                "Make ConnectedTexturesMod (CTM) work on GregTech multiblocks.",
                "Needs CTM to be loaded.",
                "Default: true"
        })
        @Config.Name("Connected textures for multiblocks")
        @Config.RequiresMcRestart
        public boolean CTM = true;
    }

    public static final class Compat {

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
        @Config.Name("Fix Alfheim v1.6+ breaking JEI")
        @Config.RequiresMcRestart
        public boolean FIX_DUMMYWORLD = true;

        @Config.Comment({
                "Fixes some GT lamps losing their inventory models when " +
                        "both VintageFix and ConnectedTexturesMod are loaded.",
                "Default: true"
        })
        @Config.Name("Fix VintageFix making lamps invisible")
        @Config.RequiresMcRestart
        public boolean FIX_LAMP_MODEL = true;

        @Config.Comment({
                "Fixes Fluidlogged API v2 making multiblocks with fluid blocks " +
                        "in the structure failed to render the in-world preview.",
                "Fixed in Fluidlogged API v3.",
                "Default: true"
        })
        @Config.Name("Fix Fluidlogged API v2 render failure")
        @Config.RequiresMcRestart
        public boolean FIX_INWORLD_PREVIEW = true;
    }

    public static final class Bugfix {

        @Config.Comment({
                "Fixes clipboards always rendered at full brightness.",
                "Default: true"
        })
        @Config.Name("Fix clipboards lighting")
        @Config.RequiresMcRestart
        public boolean FIX_CLIPBOARD = true;

        @Config.Comment({
                "Fixes facades have weird lighting.",
                "A.k.a. different from normal blocks.",
                "Default: true"
        })
        @Config.Name("Fix facades lighting")
        @Config.RequiresMcRestart
        public boolean FIX_FACADE = true;
    }

    public static final class Tweaks {

        @Config.Comment({
                "Removes the annoying search bars from GT Creative Tabs",
                "Default: true"
        })
        @Config.Name("Remove search bars from GT Tabs")
        @Config.RequiresMcRestart
        public boolean NO_BARS = true;
    }
}
