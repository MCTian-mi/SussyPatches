package dev.tianmi.sussypatches.common;

import net.minecraftforge.common.config.Config;

import com.cleanroommc.configanytime.ConfigAnytime;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.util.SusMods;

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
    public static final Tweaks TWEAK = new Tweaks();

    @Config.Comment({
            "Config options for possibly useful apis",
            "Don't enable them unless you know what you are doing"
    })
    @Config.Name("Apis")
    public static final Api API = new Api();

    public static final class Feature {

        @Config.Comment({
                "Make ConnectedTexturesMod (CTM) work on GregTech multiblocks.",
                "Needs CTM to be loaded."
        })
        @Config.Name("Connected textures for multiblocks")
        @Config.RequiresMcRestart
        public boolean multiCTM = true;

        @Config.Comment({
                "Allow you to interact with Quantum Chests with l/r-clicks.",
                "Much like that of StorageDrawers."
        })
        @Config.Name("Make Quantum Chest interactable")
        @Config.RequiresMcRestart
        public boolean interactiveStorage = true;

        @Config.Comment({
                "Render a durability bar for drums and quantum tanks, with the same color as the fluid within.",
                "Note: this won't work when you stack them.",
                "Stacking non-empty containers is buggy, would not suggest you to do that.",
                "Requires to turn on 'Enable RenderItemOverlayEvent' in the API option."
        })
        @Config.Name("Draw fluid bars for fluid container blocks")
        @Config.RequiresMcRestart
        public boolean fluidContainerBar = true;

        @Config.Comment({
                "Simply makes quantum tanks render containing fluids in their item form."
        })
        @Config.Name("Render quantum tanks fluids in inventories")
        @Config.RequiresMcRestart
        public boolean visibleStorage = true;

        @Config.Comment({
                "Add a recipe creator GUI for GroovyScript in creative mode."
        })
        @Config.Name("Recipe creator GUI for GroovyScript")
        @Config.RequiresMcRestart
        public boolean recipeCreatorGUI = true;
    }

    public static final class Compat {

        @Config.Comment({
                "Fixes GregTech machines lost their animations when " +
                        "'On-Demand Animations' is turned on in CensoredASM."
        })
        @Config.Name("Fix on-demand animations")
        @Config.RequiresMcRestart
        public boolean fixOnDemand = true;

        @Config.Comment({
                "Fixes GregTech's DummyWorld crashs with Alfheim v1.6+, " +
                        "which causes all GT recipes to disappear in JEI.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Fix Alfheim v1.6+ breaking JEI")
        @Config.RequiresMcRestart
        public boolean fixDummyWorld = true;

        @Config.Comment({
                "Fixes some GT lamps losing their inventory models when " +
                        "both VintageFix and ConnectedTexturesMod are loaded."
        })
        @Config.Name("Fix VintageFix making lamps invisible")
        @Config.RequiresMcRestart
        public boolean fixLampModel = true;

        @Config.Comment({
                "Fixes Fluidlogged API v2 making multiblocks with fluid blocks " +
                        "in the structure failed to render the in-world preview.",
                "Fixed in Fluidlogged API v3."
        })
        @Config.Name("Fix Fluidlogged API v2 render failure")
        @Config.RequiresMcRestart
        public boolean fixInworldPreview = true;

        @Config.Comment({
                "Fixes various GrS issues, including:",
                " - GrS not respecting the item namespace.",
                " - Fluid amount multiplier duplicates in the copied recipe removal code.",
                "Could also fix some CrT issues, but I didn't brother testing.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Fix various GrS issues")
        @Config.RequiresMcRestart
        public boolean fixGrS = true;

        @Config.Comment({
                "Make GT ObjectMappers support GrS inline icon.",
                "Currently only 'metaitem' mapper is supported."
        })
        @Config.Name("Inline icon for GT ObjectMappers")
        @Config.RequiresMcRestart
        public boolean inlineIcon = true;

        @Config.Comment({
                "Adds an icon in for recipes created by a tweaker mod in JEI.",
                "Backported from GregTechCEu#2638"
        })
        @Config.Name("Add tweaker icons in JEI")
        @Config.RequiresMcRestart
        public boolean tweakerInfo = true;

        @Config.Comment({
                "Stops RFTools Storage Scanner adding GT pipes as inventories."
        })
        @Config.Name("Exclude GT pipes from Storage Scanners")
        @Config.RequiresMcRestart
        public boolean noPipeForScanner = true;
    }

    public static final class Bugfix {

        @Config.Comment({
                "Fixes clipboards always rendered at full brightness."
        })
        @Config.Name("Fix clipboards lighting")
        @Config.RequiresMcRestart
        public boolean clipboardLighting = true;

        @Config.Comment({
                "Fixes facades have weird lighting.",
                "A.k.a. different from normal blocks."
        })
        @Config.Name("Fix facades lighting")
        @Config.RequiresMcRestart
        public boolean facadeLighting = true;

        @Config.Comment({
                "Implements getItem method for BlockMachine and BlockPipe.",
                "Fixes mod fail to render GT TileEntities, e.g. XNet.",
                "Note: for RFTools you will need ReFinedTools.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Implement getItem for BlockMachine")
        @Config.RequiresMcRestart
        public boolean implGetItem = true;

        @Config.Comment({
                "Fixes MTE Packet Data Memory Leak",
                "Fixed in CEu master branch."
        })
        @Config.Name("Fix packet data memory leak")
        @Config.RequiresMcRestart
        public boolean packetMemLeak = true;

        @Config.Comment({
                "Reverses the data transfer direction correctly.",
                "Fixes pipes losing data when adding/removing covers.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Fix data transfer between pipes")
        @Config.RequiresMcRestart
        public boolean pipeDataTransfer = true;

        @Config.Comment({
                "Refuses insertion into pipenets w/o specific facing.",
                "Fixes crashes with mods like RFTools."
        })
        @Config.Name("Fix data transfer between pipes")
        @Config.RequiresMcRestart
        public boolean pipeInvCrash = true;

        @Config.Comment({
                "Don't let GTCEu register pipes for empty registrations.",
                "Fixes crashes for mods like BetterQuesting.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Stop invalid pipe registration")
        @Config.RequiresMcRestart
        public boolean invalidRegistration = true;

        @Config.Comment({
                "Uses WeakReference for NeighborCacheTileEntityBase.",
                "May solve unintended JVM garbage collection hindering.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Use WeakReference for neighbor cache")
        @Config.RequiresMcRestart
        public boolean weakNeighborRef = true;

        @Config.Comment({
                "Fixes the sometimes-funny 'Gas Gas' suffix.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Removes the redundant 'Gas' suffix")
        @Config.RequiresMcRestart
        public boolean redundantGas = true;

        @Config.Comment({
                "Properly unbinds the bound framebuffer object for shaders.",
                "Potentially fixes bloom incorrectly showing through blocks.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Properly unbinding Framebuffer for shaders")
        @Config.RequiresMcRestart
        public boolean unbindFBO = true;

        @Config.Comment({
                "Removes the override to 'addDisplayText' in DistillationTower class.",
                "Which is both bugged and unnecessary.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Remove the bugged 'addDisplayText' override")
        @Config.RequiresMcRestart
        public boolean removeDTText = true;

        @Config.Comment({
                "Fix pipe frames not visible on servers.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Fix framing pipes not synced on servers")
        @Config.RequiresMcRestart
        public boolean pipeFrameDesync = true;

        @Config.Comment({
                "Fix potential 'getMinecraftServer' NPE due to TrackedDummyWorld being client-side.",
                "Fixed in CEu master branch."
        })
        @Config.Name("Fix potential MTE NPE on integrated servers")
        @Config.RequiresMcRestart
        public boolean mteServerNPE = true;

        @Config.Comment({
                "Fix RelativeDirection#LEFT and #RIGHT not treating EnumFacing#DOWN properly.",
                "Depending on the multi impl this may or may not fix things."
        })
        @Config.Name("Fix RelativeDirection providing wrong facings")
        @Config.RequiresMcRestart
        public boolean relativeDir = true;

        @Config.Comment({
                "Make GT TileEntities aware of nearby chunk loading/unloadings.",
                "Fixes issues like fluid pipes disconnecting at chunk border after chunk unload."
        })
        @Config.Name("Fix GT tiles not chunk-aware")
        @Config.RequiresMcRestart
        public boolean chunkAware = true;

        @Config.Comment({
                "Fixes an edge case for cleanroom structure check.",
                "You can now put hatches at the same axis as the controller on the top layer."
        })
        @Config.Name("Fix Cleanroom structure check")
        @Config.RequiresMcRestart
        public boolean cleanroomStruct = true;

        @Config.Comment({
                "Fixes in-world preview missing blocks.",
                "E.g. for Cleanroom."
        })
        @Config.Name("Fix multiblock preview missing blocks")
        @Config.RequiresMcRestart
        public boolean previewMissingBlocks = true;

        @Config.Comment({
                "Fixes GregTech Crafting Station voiding fluid containers during batch crafting."
        })
        @Config.Name("Fix crafting station voiding fluid containers")
        @Config.RequiresMcRestart
        public boolean workbenchVoidContainers = true;

        @Config.Comment({
                "Fixes quadruple and nonuple pipes have rendering issues, like weird lighting, etc.",
                "Note: this fix simply shrinks their thickness from 0.95 to 0.9375, a magic number that just works."
        })
        @Config.Name("Fix quad/nonuple pipes rendering")
        @Config.RequiresMcRestart
        public boolean thickPipeRender = true;
    }

    public static final class Tweaks {

        @Config.Comment({
                "Removes the annoying search bars from GT Creative Tabs."
        })
        @Config.Name("Remove search bars from GT tabs")
        @Config.RequiresMcRestart
        public boolean noSearchBars = true;

        @Config.Comment({
                "Switches from GT's own impl of XSTR to XoShiRo256++ random generator.",
                "Which has both better performance and better randomness.",
                "Backported from GregTechCEu#2747"
        })
        @Config.Name("Use XoShiRo256++ Random")
        @Config.RequiresMcRestart
        public boolean xoShiRo256plusplus = true;

        @Config.Comment({
                "Uses VBO for JEI preview renderer, which would significantly reduce the render lag.",
                "Also removes the ISceneRenderHook logic from the WorldSceneRenderer, which reduces the allocation in multiblock previews.",
                "Note: has some minor issues with the terminal.",
                "Backported from GregTechCEu#2629"
        })
        @Config.Name("Optimize JEI multiblock preview")
        @Config.RequiresMcRestart
        public boolean optPreview = true;

        @Config.Comment({
                "Removes the muffler inventory and recovery mechanics.",
                "Who would ever use them, anyways.",
                "Would help with performance, in a way.",
                "Backported from GregTechCEu#2799 and Nomi-Libs"
        })
        @Config.Name("Disable the muffler recovery mechanic")
        @Config.RequiresMcRestart
        public boolean noMufflerRecovery = true;

        @Config.Comment({
                "Make prospectors place waypoints at the average height of the hovered ores.",
                "Less tedious than having to look it up in jei or blindly mining up or down.",
                "Backported from GregTechCEu#2726"
        })
        @Config.Name("Place ore prospector waypoints at vein height")
        @Config.RequiresMcRestart
        public boolean prospectorHeight = true;

        @Config.Comment({
                "Doubles the thickness of covers on a pipe.",
                "So that they look the same as in GT5."
        })
        @Config.Name("Make covers on a pipe thicker")
        @Config.RequiresMcRestart
        public boolean thickerCovers = true;

        @Config.Comment({
                "Makes GT machine items render their active-state textures.",
                "So that they look the same as in GT5."
        })
        @Config.Name("Render active textures for GT machine items")
        @Config.RequiresMcRestart
        public boolean activeMTEItems = false;

        @Config.Comment({
                "Add all GT tools made of different materials to JEI.",
                "Also separating their recipes.",
                "Note: CEu code here is really shitty, use at your own risk."
        })
        @Config.Name("Show all GT tools in JEI")
        @Config.RequiresMcRestart
        public boolean showAllToolItems = false;

        @Config.Comment({
                "Replaces the text for Creative Chest/Tanks.",
                "Leave this empty to use default numbers."
        })
        @Config.Name("Replace Creative Chest/Tank display text")
        @Config.RequiresMcRestart
        public String cStorageInf = "";

        @Config.Comment({
                "Basically just give GT blocks and MetaTileEntities proper sound types based on their materials.",
                "Backported from GregTechCEu#2853"
        })
        @Config.Name("Give GT blocks & MTEs proper step sounds")
        @Config.RequiresMcRestart
        public boolean customMTESounds = true;

        @Config.Comment({
                "Literally. Just made it possible for mobs to spawn on GT stone blocks.",
                "Backported from GregTechCEu#2859"
        })
        @Config.Name("Allow mob spawning on GT stones")
        @Config.RequiresMcRestart
        public boolean mobSpawnOnStones = true;
    }

    public static final class Api {

        @Config.Comment({
                "Supports using ModularUI2 for GT MTEs.",
                "Note: there's no default impl for existing MTEs, currently.",
                "Backported from GregTechCEu#2281"
        })
        @Config.Name("Enable ModularUI2 support")
        @Config.RequiresMcRestart
        public boolean useMui2 = SusMods.DevEnv.isLoaded();

        @Config.Comment({
                "Basically just added an event for handle item overlay rendering.",
                "Technically this isn't anything strictly bounded to GregTech, you can use it wherever you want.",
                "But well this is needed for the 'Draw fluid bar for drums' feature."
        })
        @Config.Name("Enable RenderItemOverlayEvent")
        @Config.RequiresMcRestart
        @Config.Ignore // TODO: fix early config
        public boolean itemOverlayEvent = true;

        @Config.Comment({
                "Let all material cable and pipe textures be configurable with its icon set texture.",
                "Note: if this option is enabled, then the correspondence textures in default path will invalid,",
                "only the textures in its icon set path will be rendered."
        })
        @Config.Name("Render cable and pipe textures from its icon set")
        @Config.RequiresMcRestart
        public boolean pipeIconTypes = true;
    }

    static {
        if (SusMods.ConfigAnytime.isLoaded()) {
            ConfigAnytime.register(SusConfig.class);
        } else if (!SusMods.Lwjgl3ify.isLoaded()) { // Can't run ConfigAnytime on Lwjgl3ify environment
            SussyPatches.LOGGER.error("ConfigAnytime not found! Configurations may not work!");
        }
    }
}
