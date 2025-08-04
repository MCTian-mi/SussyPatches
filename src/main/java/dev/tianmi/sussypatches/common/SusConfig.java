package dev.tianmi.sussypatches.common;

import net.minecraftforge.common.config.Config;

import dev.tianmi.sussypatches.Tags;

@Config(modid = Tags.MODID)
public class SusConfig {

    @Config.Comment("Config options for additional features")
    @Config.Name("Features")
    public static final Feature FEAT = new Feature();

    @Config.Comment("Config options for external mod compat")
    @Config.Name("Compatibilities")
    public static final Compat COMPAT = new Compat();

    @Config.Comment("Config options for fixing... bugs")
    @Config.Name("Bugfixes")
    public static final Bugfix BUGFIX = new Bugfix();

    @Config.Comment("Config options for tweaking existing features")
    @Config.Name("Tweaks")
    public static final Tweaks TWEAKS = new Tweaks();

    public static final class Feature {

        @Config.Comment({
                "Make ConnectedTexturesMod (CTM) work on GregTech multiblocks.",
                "Needs CTM to be loaded.",
                "Default: true"
        })
        @Config.Name("Connected textures for multiblocks")
        @Config.RequiresMcRestart
        public boolean multiCTM = true;
    }

    public static final class Compat {

        @Config.Comment({
                "Fixes GregTech machines lost their animations when " +
                        "\"On-Demand Animations\" is turned on in CensoredASM.",
                "Default: true"
        })
        @Config.Name("Fix on-demand animations")
        @Config.RequiresMcRestart
        public boolean fixOnDemand = true;

        @Config.Comment({
                "Fixes GregTech's DummyWorld crashs with Alfheim v1.6+, " +
                        "which causes all GT recipes to disappear in JEI.",
                "Fixed in CEu master branch.",
                "Default: true"
        })
        @Config.Name("Fix Alfheim v1.6+ breaking JEI")
        @Config.RequiresMcRestart
        public boolean fixDummyWorld = true;

        @Config.Comment({
                "Fixes some GT lamps losing their inventory models when " +
                        "both VintageFix and ConnectedTexturesMod are loaded.",
                "Default: true"
        })
        @Config.Name("Fix VintageFix making lamps invisible")
        @Config.RequiresMcRestart
        public boolean fixLampModel = true;

        @Config.Comment({
                "Fixes Fluidlogged API v2 making multiblocks with fluid blocks " +
                        "in the structure failed to render the in-world preview.",
                "Fixed in Fluidlogged API v3.",
                "Default: true"
        })
        @Config.Name("Fix Fluidlogged API v2 render failure")
        @Config.RequiresMcRestart
        public boolean fixInworldPreview = true;

        @Config.Comment({
                "Fixes various GrS issues, including:",
                " - GrS not respecting the item namespace.",
                " - Fluid amount multiplier duplicates in the copied recipe removal code.",
                "Could also fix some CrT issues, but I didn't brother testing.",
                "Fixed in CEu master branch.",
                "Default: true"
        })
        @Config.Name("Fix various GrS issues")
        @Config.RequiresMcRestart
        public boolean fixGrS = true;
    }

    public static final class Bugfix {

        @Config.Comment({
                "Fixes clipboards always rendered at full brightness.",
                "Default: true"
        })
        @Config.Name("Fix clipboards lighting")
        @Config.RequiresMcRestart
        public boolean clipboardLighting = true;

        @Config.Comment({
                "Fixes facades have weird lighting.",
                "A.k.a. different from normal blocks.",
                "Default: true"
        })
        @Config.Name("Fix facades lighting")
        @Config.RequiresMcRestart
        public boolean facadeLighting = true;

        @Config.Comment({
                "Implements getItem method for BlockMachine.",
                "Fixes mod fail to render GT TileEntities, e.g. XNet.",
                "Note: for RFTools you will need ReFinedTools.",
                "Fixed in CEu master branch.",
                "Default: true"
        })
        @Config.Name("Implement getItem for BlockMachine")
        @Config.RequiresMcRestart
        public boolean implGetItem = true;

        @Config.Comment({
                "Fixes MTE Packet Data Memory Leak",
                "Fixed in CEu master branch.",
                "Default: true"
        })
        @Config.Name("Fix packet data memory leak")
        @Config.RequiresMcRestart
        public boolean packetMemLeak = true;

        @Config.Comment({
                "Reverses the data transfer direction correctly.",
                "Fixes pipes losing data when adding/removing covers.",
                "Fixed in CEu master branch.",
                "Default: true"
        })
        @Config.Name("Fix data transfer between pipes")
        @Config.RequiresMcRestart
        public boolean pipeDataTransfer = true;

        @Config.Comment({
                "Refuses insertion into pipenets w/o specific facing.",
                "Fixes crashing with mods like RFTools.",
                "Default: true"
        })
        @Config.Name("Fix data transfer between pipes")
        @Config.RequiresMcRestart
        public boolean pipeInvCrash = true;
    }

    public static final class Tweaks {

        @Config.Comment({
                "Removes the annoying search bars from GT Creative Tabs",
                "Default: true"
        })
        @Config.Name("Remove search bars from GT Tabs")
        @Config.RequiresMcRestart
        public boolean noSearchBars = true;
    }
}
