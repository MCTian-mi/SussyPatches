package dev.tianmi.sussypatches.common;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;

import com.cleanroommc.configanytime.ConfigAnytime;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.util.SusMods;

@Config(modid = Tags.MODID)
public class SusConfig {

    @Comment("Config options for additional features")
    @Name("Features")
    public static final Feature FEAT = new Feature();

    @Comment("Config options for external mod compat")
    @Name("Compatibilities")
    public static final Compat COMPAT = new Compat();

    @Comment("Config options for fixing... bugs")
    @Name("Bugfixes")
    public static final Bugfix BUGFIX = new Bugfix();

    @Comment("Config options for tweaking existing features")
    @Name("Tweaks")
    public static final Tweaks TWEAK = new Tweaks();

    @Comment({
            "Config options for possibly useful apis",
            "Don't enable them unless you know what you are doing"
    })
    @Name("Apis")
    public static final Api API = new Api();

    public static final class Feature {

        @Comment({
                "Make ConnectedTexturesMod (CTM) work on GregTech multiblocks.",
                "Needs CTM to be loaded."
        })
        @Name("Connected textures for multiblocks")
        @RequiresMcRestart
        public boolean multiCTM = true;

        @Comment({
                "Allow you to interact with Quantum Chests with l/r-clicks.",
                "Much like that of StorageDrawers."
        })
        @Name("Make Quantum Chest interactable")
        @RequiresMcRestart
        public boolean interactiveStorage = true;

        @Comment({
                "Render a durability bar for drums and quantum tanks, " +
                        "with the same color as the fluid within.",
                "Note: this won't work when you stack them.",
                "Stacking non-empty containers is buggy, would not suggest you to do that.",
                "Requires to turn on 'Enable RenderItemOverlayEvent' in the API option."
        })
        @Name("Draw fluid bars for fluid container blocks")
        @RequiresMcRestart
        public boolean fluidContainerBar = true;

        @Comment({
                "Simply makes quantum tanks render containing fluids in their item form."
        })
        @Name("Render quantum tanks fluids in inventories")
        @RequiresMcRestart
        public boolean visibleStorage = true;

        @Comment({
                "Allows maintaining multiblocks with tools from containers in the player's inventory.",
                "E.g., tool belt or backpacks.",
                "Note: may slightly affect performance."
        })
        @Name("Reach deeper in your pocket when maintaining multis")
        @RequiresMcRestart
        public boolean deepMaintenance = true;

        @Comment({
                "Adds the retain exact mode to fluid regulators and robot arms.",
                "It keeps specified amount of item/fluids in the source container.",
                "Backported from GregTechCEu#2684",
                "CAUTION: You will HAVE to change this into other transfer modes in your covers " +
                        "before disabling this option or removing this mod. Otherwise your machines may got evaporated.",
        })
        @Name("Reach deeper in your pocket when maintaining multis")
        @RequiresMcRestart
        public boolean coverRetainExact = true;
    }

    public static final class Compat {

        @Comment({
                "Fixes GregTech machines lost their animations when " +
                        "'On-Demand Animations' is turned on in CensoredASM."
        })
        @Name("Fix on-demand animations")
        @RequiresMcRestart
        public boolean fixOnDemand = true;

        @Comment({
                "Fixes GregTech's DummyWorld crashs with Alfheim v1.6+, " +
                        "which causes all GT recipes to disappear in JEI.",
                "Fixed in CEu master branch."
        })
        @Name("Fix Alfheim v1.6+ breaking JEI")
        @RequiresMcRestart
        public boolean fixDummyWorld = true;

        @Comment({
                "Fixes some GT lamps losing their inventory models when " +
                        "both VintageFix and ConnectedTexturesMod are loaded."
        })
        @Name("Fix VintageFix making lamps invisible")
        @RequiresMcRestart
        public boolean fixLampModel = true;

        @Comment({
                "Fixes Fluidlogged API v2 making multiblocks with fluid blocks " +
                        "in the structure failed to render the in-world preview.",
                "Fixed in Fluidlogged API v3."
        })
        @Name("Fix Fluidlogged API v2 render failure")
        @RequiresMcRestart
        public boolean fixInworldPreview = true;

        @Comment({
                "Fixes various GrS issues, including:",
                " - GrS not respecting the item namespace.",
                " - Fluid amount multiplier duplicates in the copied recipe removal code.",
                "Could also fix some CrT issues, but I didn't brother testing.",
                "Fixed in CEu master branch."
        })
        @Name("Fix various GrS issues")
        @RequiresMcRestart
        public boolean fixGrS = true;

        @Comment({
                "Make GT ObjectMappers support GrS inline icon.",
                "Currently only 'metaitem' mapper is supported."
        })
        @Name("Inline icon for GT ObjectMappers")
        @RequiresMcRestart
        public boolean inlineIcon = true;

        @Comment({
                "Adds an icon in for recipes created by a tweaker mod in JEI.",
                "Backported from GregTechCEu#2638"
        })
        @Name("Add tweaker icons in JEI")
        @RequiresMcRestart
        public boolean tweakerInfo = true;

        @Comment({
                "Stops RFTools Storage Scanner adding GT pipes as inventories."
        })
        @Name("Exclude GT pipes from Storage Scanners")
        @RequiresMcRestart
        public boolean noPipeForScanner = true;

        @Comment({
                "Add a recipe creator GUI for GroovyScript in creative mode."
        })
        @Name("In-Game GroovyScript recipe creator")
        @RequiresMcRestart
        public boolean grsRecipeCreator = true;
    }

    public static final class Bugfix {

        @Comment({
                "Fixes clipboards always rendered at full brightness."
        })
        @Name("Fix clipboards lighting")
        @RequiresMcRestart
        public boolean clipboardLighting = true;

        @Comment({
                "Fixes facades have weird lighting.",
                "A.k.a. different from normal blocks."
        })
        @Name("Fix facades lighting")
        @RequiresMcRestart
        public boolean facadeLighting = true;

        @Comment({
                "Implements getItem method for BlockMachine and BlockPipe.",
                "Fixes mod fail to render GT TileEntities, e.g. XNet.",
                "Note: for RFTools you will need ReFinedTools.",
                "Fixed in CEu master branch."
        })
        @Name("Implement getItem for BlockMachine")
        @RequiresMcRestart
        public boolean implGetItem = true;

        @Comment({
                "Fixes MTE Packet Data Memory Leak",
                "Fixed in CEu master branch."
        })
        @Name("Fix packet data memory leak")
        @RequiresMcRestart
        public boolean packetMemLeak = true;

        @Comment({
                "Reverses the data transfer direction correctly.",
                "Fixes pipes losing data when adding/removing covers.",
                "Fixed in CEu master branch."
        })
        @Name("Fix data transfer between pipes")
        @RequiresMcRestart
        public boolean pipeDataTransfer = true;

        @Comment({
                "Refuses insertion into pipenets w/o specific facing.",
                "Fixes crashes with mods like RFTools."
        })
        @Name("Fix data transfer between pipes")
        @RequiresMcRestart
        public boolean pipeInvCrash = true;

        @Comment({
                "Don't let GTCEu register pipes for empty registrations.",
                "Fixes crashes for mods like BetterQuesting.",
                "Fixed in CEu master branch."
        })
        @Name("Stop invalid pipe registration")
        @RequiresMcRestart
        public boolean invalidRegistration = true;

        @Comment({
                "Uses WeakReference for NeighborCacheTileEntityBase.",
                "May solve unintended JVM garbage collection hindering.",
                "Fixed in CEu master branch."
        })
        @Name("Use WeakReference for neighbor cache")
        @RequiresMcRestart
        public boolean weakNeighborRef = true;

        @Comment({
                "Fixes the sometimes-funny 'Gas Gas' suffix.",
                "Fixed in CEu master branch."
        })
        @Name("Removes the redundant 'Gas' suffix")
        @RequiresMcRestart
        public boolean redundantGas = true;

        @Comment({
                "Properly unbinds the bound framebuffer object for shaders.",
                "Potentially fixes bloom incorrectly showing through blocks.",
                "Fixed in CEu master branch."
        })
        @Name("Properly unbinding Framebuffer for shaders")
        @RequiresMcRestart
        public boolean unbindFBO = true;

        @Comment({
                "Removes the override to 'addDisplayText' in DistillationTower class.",
                "Which is both bugged and unnecessary.",
                "Fixed in CEu master branch."
        })
        @Name("Remove the bugged 'addDisplayText' override")
        @RequiresMcRestart
        public boolean removeDTText = true;

        @Comment({
                "Fix pipe frames not visible on servers.",
                "Fixed in CEu master branch."
        })
        @Name("Fix framing pipes not synced on servers")
        @RequiresMcRestart
        public boolean pipeFrameDesync = true;

        @Comment({
                "Fix potential 'getMinecraftServer' NPE due to TrackedDummyWorld being client-side.",
                "Fixed in CEu master branch."
        })
        @Name("Fix potential MTE NPE on integrated servers")
        @RequiresMcRestart
        public boolean mteServerNPE = true;

        @Comment({
                "Fix RelativeDirection#LEFT and #RIGHT not treating EnumFacing#DOWN properly.",
                "Depending on the multi impl this may or may not fix things."
        })
        @Name("Fix RelativeDirection providing wrong facings")
        @RequiresMcRestart
        public boolean relativeDir = true;

        @Comment({
                "Make GT TileEntities aware of nearby chunk loading/unloadings.",
                "Fixes issues like fluid pipes disconnecting at chunk border after chunk unload."
        })
        @Name("Fix GT tiles not chunk-aware")
        @RequiresMcRestart
        public boolean chunkAware = true;

        @Comment({
                "Fixes an edge case for cleanroom structure check.",
                "You can now put hatches at the same axis as the controller on the top layer."
        })
        @Name("Fix Cleanroom structure check")
        @RequiresMcRestart
        public boolean cleanroomStruct = true;

        @Comment({
                "Fixes in-world preview missing blocks.",
                "E.g. for Cleanroom."
        })
        @Name("Fix multiblock preview missing blocks")
        @RequiresMcRestart
        public boolean previewMissingBlocks = true;

        @Comment({
                "Fixes GregTech Crafting Station voiding fluid containers during batch crafting."
        })
        @Name("Fix crafting station voiding fluid containers")
        @RequiresMcRestart
        public boolean workbenchVoidContainers = true;

        @Comment({
                "Fixes quadruple and nonuple pipes have rendering issues, like weird lighting, etc.",
                "Note: this fix simply shrinks their thickness from 0.95 to 0.9375, a magic number that just works."
        })
        @Name("Fix quad/nonuple pipes rendering")
        @RequiresMcRestart
        public boolean thickPipeRender = true;

        @Comment({
                "Literally. Just made it possible for mobs to spawn on GT stone blocks.",
                "Backported from GregTechCEu#2859"
        })
        @Name("Fix mob not spawning on GT stones")
        @RequiresMcRestart
        public boolean mobSpawnOnStones = true;
    }

    public static final class Tweaks {

        @Comment({
                "Removes the annoying search bars from GT Creative Tabs."
        })
        @Name("Remove search bars from GT tabs")
        @RequiresMcRestart
        public boolean noSearchBars = true;

        @Comment({
                "Switches from GT's own impl of XSTR to XoShiRo256++ random generator.",
                "Which has both better performance and better randomness.",
                "Backported from GregTechCEu#2747"
        })
        @Name("Use XoShiRo256++ Random")
        @RequiresMcRestart
        public boolean xoShiRo256plusplus = true;

        @Comment({
                "Uses VBO for JEI preview renderer, which would significantly reduce the render lag.",
                "Also removes the ISceneRenderHook logic from the WorldSceneRenderer, which reduces the allocation in multiblock previews.",
                "Note: has some minor issues with the terminal.",
                "Backported from GregTechCEu#2629"
        })
        @Name("Optimize JEI multiblock preview")
        @RequiresMcRestart
        public boolean optPreview = true;

        @Comment({
                "Removes the muffler inventory and recovery mechanics.",
                "Who would ever use them, anyways.",
                "Would help with performance, in a way.",
                "Backported from GregTechCEu#2799 and Nomi-Libs"
        })
        @Name("Disable the muffler recovery mechanic")
        @RequiresMcRestart
        public boolean noMufflerRecovery = true;

        @Comment({
                "Make prospectors place waypoints at the average height of the hovered ores.",
                "Less tedious than having to look it up in jei or blindly mining up or down.",
                "Backported from GregTechCEu#2726"
        })
        @Name("Place ore prospector waypoints at vein height")
        @RequiresMcRestart
        public boolean prospectorHeight = true;

        @Comment({
                "Doubles the thickness of covers on a pipe.",
                "So that they look the same as in GT5."
        })
        @Name("Make covers on a pipe thicker")
        @RequiresMcRestart
        public boolean thickerCovers = true;

        @Comment({
                "Makes GT machine items render their active-state textures.",
                "So that they look the same as in GT5."
        })
        @Name("Render active textures for GT machine items")
        @RequiresMcRestart
        public boolean activeMTEItems = false;

        @Comment({
                "Add all GT tools made of different materials to JEI.",
                "Also separating their recipes.",
                "Note: CEu code here is really shitty, use at your own risk."
        })
        @Name("Show all GT tools in JEI")
        @RequiresMcRestart
        public boolean showAllToolItems = false;

        @Comment({
                "Replaces the text for Creative Chest/Tanks.",
                "Leave this empty to use default numbers."
        })
        @Name("Replace Creative Chest/Tank display text")
        @RequiresMcRestart
        public String cStorageInf = "";

        @Comment({
                "Basically just give GT blocks and MetaTileEntities proper sound types based on their materials.",
                "Backported from GregTechCEu#2853"
        })
        @Name("Give GT blocks & MTEs proper step sounds")
        @RequiresMcRestart
        public boolean customMTESounds = true;
    }

    public static final class Api {

        @Comment({
                "Supports using ModularUI2 for GT MTEs.",
                "Note: there's no default impl for existing MTEs, currently.",
                "Backported from GregTechCEu#2281"
        })
        @Name("Enable ModularUI2 support")
        @RequiresMcRestart
        public boolean useMui2 = SusMods.DevEnv.isLoaded();

        @Comment({
                "Basically just added an event for handle item overlay rendering.",
                "Technically this isn't anything strictly bounded to GregTech, you can use it wherever you want.",
                "But well this is needed for the 'Draw fluid bar for drums' feature."
        })
        @Name("Enable RenderItemOverlayEvent")
        @RequiresMcRestart
        @Ignore // TODO)) fix early config
        public boolean itemOverlayEvent = true;

        @Comment({
                "Let all material cable and pipe textures be configurable with its icon set texture.",
                "Note: if this option is enabled, then the correspondence textures in default path will invalid,",
                "only the textures in its icon set path will be rendered."
        })
        @Name("Render cable and pipe textures from its icon set")
        @RequiresMcRestart
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
