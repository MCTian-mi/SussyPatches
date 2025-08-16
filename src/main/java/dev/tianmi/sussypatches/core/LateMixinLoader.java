package dev.tianmi.sussypatches.core;

import static dev.tianmi.sussypatches.core.LateMixinLoader.Type.*;
import static gregtech.api.util.Mods.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.util.BoolSupplier;
import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.common.SusConfig;
import zone.rong.mixinbooter.ILateMixinLoader;

@SuppressWarnings("unused")
public class LateMixinLoader implements ILateMixinLoader {

    private static final Map<String, BoolSupplier> MIXIN_CONFIGS = new LinkedHashMap<>();

    static {
        FEATURE.add("connectedtextures", SusMods.of(CTM), SusConfig.FEAT.multiCTM);
        FEATURE.add("interactivestorage", SusConfig.FEAT.interactiveStorage);

        COMPAT.add("ondemandanimation", SusMods.LoliASM, SusConfig.COMPAT.fixOnDemand);
        COMPAT.add("dummyworldcrash", SusMods.of(Alfheim), SusConfig.COMPAT.fixDummyWorld);
        COMPAT.add("lampbakedmodel", SusMods.VintageFix, SusMods.of(CTM), SusConfig.COMPAT.fixLampModel);
        COMPAT.add("inworldpreviewcrash", SusMods.FluidloggedAPI_2, SusConfig.COMPAT.fixInworldPreview);
        COMPAT.add("variousgrsissue", SusMods.of(GroovyScript), SusMods.NomiLibs.negate(),
                SusConfig.COMPAT.fixGrS);
        COMPAT.add("grsinlineicon", SusMods.of(GroovyScript), SusConfig.COMPAT.inlineIcon);
        COMPAT.add("tweakerinfo", SusMods.of(GroovyScript).or(SusMods.of(CraftTweaker)),
                SusMods.of(JustEnoughItems), SusConfig.COMPAT.tweakerInfo);

        BUGFIX.add("clipboardlighting", SusConfig.BUGFIX.clipboardLighting);
        BUGFIX.add("facadelighting", SusConfig.BUGFIX.facadeLighting);
        BUGFIX.add("implgetitem", SusConfig.BUGFIX.implGetItem);
        BUGFIX.add("packetdatamemleak", SusConfig.BUGFIX.packetMemLeak);
        BUGFIX.add("pipedatatransfer", SusMods.NomiLibs.negate(), SusConfig.BUGFIX.pipeDataTransfer);
        BUGFIX.add("pipeinvcrash", SusConfig.BUGFIX.pipeInvCrash);
        BUGFIX.add("invalidregistration", SusConfig.BUGFIX.invalidRegistration);
        BUGFIX.add("weakneighborref", SusConfig.BUGFIX.weakNeighborRef);
        BUGFIX.add("redundantgas", SusMods.NomiLibs.negate(), SusConfig.BUGFIX.redundantGas);
        BUGFIX.add("unbindframebuffer", SusConfig.BUGFIX.unbindFBO);
        BUGFIX.add("dtguitext", SusConfig.BUGFIX.removeDTText);
        BUGFIX.add("pipeframedesync", SusConfig.BUGFIX.pipeFrameDesync);
        BUGFIX.add("mtenpeonserver", SusConfig.BUGFIX.mteServerNPE);
        BUGFIX.add("relativedirection", SusConfig.BUGFIX.relativeDir);
        BUGFIX.add("chunkaware", SusConfig.BUGFIX.chunkAware);
        BUGFIX.add("cleanroomstructure", SusMods.NomiLibs.negate(), SusConfig.BUGFIX.cleanroomStruct);
        BUGFIX.add("previewmissingblocks", SusConfig.BUGFIX.previewMissingBlocks);

        TWEAK.add("tabnosearchbars", SusConfig.TWEAK.noSearchBars);
        TWEAK.add("xoshiro256plusplus", SusConfig.TWEAK.xoShiRo256plusplus);
        TWEAK.add("previewoptimization", SusConfig.TWEAK.optPreview);
        TWEAK.add("nomuffler", SusMods.NomiLibs.negate(), SusConfig.TWEAK.noMufflerRecovery);
        TWEAK.add("prospectorheight", SusMods.NomiLibs.negate(), SusConfig.TWEAK.prospectorHeight);
        TWEAK.add("thickercovers", SusConfig.TWEAK.thickerCovers);
        TWEAK.add("activemteitems", SusConfig.TWEAK.activeMTEItems);
        TWEAK.add("toolsubitems", SusConfig.TWEAK.showAllToolItems);

        API.add("usemui2", SusMods.ModularUI, SusConfig.API.useMui2);
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
            BoolSupplier supplier = BoolSupplier.TRUE;
            for (var condition : conditions) {
                if (condition instanceof BoolSupplier boolSupplier) {
                    supplier = supplier.and(boolSupplier);
                } else if (condition instanceof Boolean bool) {
                    supplier = supplier.and(BoolSupplier.of(bool));
                } else if (condition instanceof BooleanSupplier booleanSupplier) {
                    supplier = supplier.and(BoolSupplier.of(booleanSupplier));
                } else {
                    throw new IllegalArgumentException("Invalid condition type: " + condition.getClass());
                }
            }
            MIXIN_CONFIGS.put(ROOT + this + MIXINS + name + JSON, supplier);
        }
    }
}
