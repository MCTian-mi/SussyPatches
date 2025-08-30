package dev.tianmi.sussypatches.core;

import static dev.tianmi.sussypatches.core.LateMixinLoader.Type.*;
import static gregtech.api.util.Mods.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.util.BoolSupplier;
import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.common.SusConfig;
import zone.rong.mixinbooter.ILateMixinLoader;

@SuppressWarnings("unused")
public class LateMixinLoader implements ILateMixinLoader {

    private static final Map<String, BoolSupplier> MIXIN_CONFIGS = new LinkedHashMap<>();

    static {
        FEATURE.add("connectedtextures", SusConfig.FEAT.multiCTM, SusMods.of(CTM));
        FEATURE.add("interactivestorage", SusConfig.FEAT.interactiveStorage);
        FEATURE.add("fluidcontainerbar", SusConfig.FEAT.fluidContainerBar, SusConfig.API.itemOverlayEvent);
        FEATURE.add("visiblestorage", SusConfig.FEAT.visibleStorage);
        FEATURE.add("grsrecipecreator", SusConfig.FEAT.grsRecipeCreator);

        COMPAT.add("ondemandanimation", SusConfig.COMPAT.fixOnDemand, SusMods.LoliASM);
        COMPAT.add("dummyworldcrash", SusConfig.COMPAT.fixDummyWorld, SusMods.of(Alfheim));
        COMPAT.add("lampbakedmodel", SusConfig.COMPAT.fixLampModel, SusMods.VintageFix, SusMods.of(CTM));
        COMPAT.add("inworldpreviewcrash", SusConfig.COMPAT.fixInworldPreview, SusMods.FluidloggedAPI_2);
        COMPAT.add("variousgrsissue", SusConfig.COMPAT.fixGrS, SusMods.of(GroovyScript),
                SusMods.NomiLibs.negate());
        COMPAT.add("grsinlineicon", SusConfig.COMPAT.inlineIcon, SusMods.of(GroovyScript));
        COMPAT.add("tweakerinfo", SusConfig.COMPAT.tweakerInfo, SusMods.of(GroovyScript)
                .or(SusMods.of(CraftTweaker)), SusMods.of(JustEnoughItems));
        COMPAT.add("nopipeforscanner", SusConfig.COMPAT.noPipeForScanner, SusMods.RFTools);

        BUGFIX.add("clipboardlighting", SusConfig.BUGFIX.clipboardLighting);
        BUGFIX.add("facadelighting", SusConfig.BUGFIX.facadeLighting);
        BUGFIX.add("implgetitem", SusConfig.BUGFIX.implGetItem);
        BUGFIX.add("packetdatamemleak", SusConfig.BUGFIX.packetMemLeak);
        BUGFIX.add("pipedatatransfer", SusConfig.BUGFIX.pipeDataTransfer, SusMods.NomiLibs.negate());
        BUGFIX.add("pipeinvcrash", SusConfig.BUGFIX.pipeInvCrash);
        BUGFIX.add("invalidregistration", SusConfig.BUGFIX.invalidRegistration);
        BUGFIX.add("weakneighborref", SusConfig.BUGFIX.weakNeighborRef);
        BUGFIX.add("redundantgas", SusConfig.BUGFIX.redundantGas, SusMods.NomiLibs.negate());
        BUGFIX.add("unbindframebuffer", SusConfig.BUGFIX.unbindFBO);
        BUGFIX.add("dtguitext", SusConfig.BUGFIX.removeDTText);
        BUGFIX.add("pipeframedesync", SusConfig.BUGFIX.pipeFrameDesync);
        BUGFIX.add("mtenpeonserver", SusConfig.BUGFIX.mteServerNPE);
        BUGFIX.add("relativedirection", SusConfig.BUGFIX.relativeDir);
        BUGFIX.add("chunkaware", SusConfig.BUGFIX.chunkAware);
        BUGFIX.add("cleanroomstructure", SusConfig.BUGFIX.cleanroomStruct, SusMods.NomiLibs.negate());
        BUGFIX.add("previewmissingblocks", SusConfig.BUGFIX.previewMissingBlocks);
        BUGFIX.add("workbenchvoidcontainers", SusConfig.BUGFIX.workbenchVoidContainers);
        BUGFIX.add("thickpiperender", SusConfig.BUGFIX.thickPipeRender);

        TWEAK.add("tabnosearchbars", SusConfig.TWEAK.noSearchBars);
        TWEAK.add("xoshiro256plusplus", SusConfig.TWEAK.xoShiRo256plusplus);
        TWEAK.add("previewoptimization", SusConfig.TWEAK.optPreview);
        TWEAK.add("nomuffler", SusConfig.TWEAK.noMufflerRecovery, SusMods.NomiLibs.negate());
        TWEAK.add("prospectorheight", SusConfig.TWEAK.prospectorHeight, SusMods.NomiLibs.negate());
        TWEAK.add("thickercovers", SusConfig.TWEAK.thickerCovers);
        TWEAK.add("activemteitems", SusConfig.TWEAK.activeMTEItems);
        TWEAK.add("toolsubitems", SusConfig.TWEAK.showAllToolItems);
        TWEAK.add("cstorageinf", !SusConfig.TWEAK.cStorageInf.isEmpty());
        TWEAK.add("customsoundtype", SusConfig.TWEAK.customMTESounds);
        TWEAK.add("mobspawnonstones", SusConfig.TWEAK.mobSpawnOnStones);

        API.add("usemui2", SusConfig.API.useMui2, SusMods.ModularUI);
        API.add("pipeicontypes", SusConfig.API.pipeIconTypes);
    }

    public static int foo() {
        SussyPatches.LOGGER.info("dashdaoisdaoh");
        return 0;
    }

    @Override
    public List<String> getMixinConfigs() {
        return new ArrayList<>(MIXIN_CONFIGS.keySet());
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return MIXIN_CONFIGS.get(mixinConfig).get();
    }

    enum Type {

        FEATURE,
        BUGFIX,
        TWEAK,
        COMPAT,
        API,
        ;

        private static final String ROOT = Tags.MODID + "/";
        private static final String MIXINS = "mixins.";
        private static final String JSON = ".json";

        @Override
        public String toString() {
            return name().toLowerCase() + "/";
        }

        public void add(String name, Object... conditions) {
            MIXIN_CONFIGS.put(ROOT + this + MIXINS + name + JSON, BoolSupplier.compact(conditions));
        }
    }
}
