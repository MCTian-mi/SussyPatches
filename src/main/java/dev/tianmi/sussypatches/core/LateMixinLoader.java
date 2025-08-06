package dev.tianmi.sussypatches.core;

import static dev.tianmi.sussypatches.core.LateMixinLoader.Type.*;
import static gregtech.api.util.Mods.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        COMPAT.add("ondemandanimation", SusMods.LoliASM, SusConfig.COMPAT.fixOnDemand);
        COMPAT.add("dummyworldcrash", SusMods.of(Alfheim), SusConfig.COMPAT.fixDummyWorld);
        COMPAT.add("lampbakedmodel", SusMods.VintageFix, SusMods.of(CTM), SusConfig.COMPAT.fixLampModel);
        COMPAT.add("inworldpreviewcrash", SusMods.FluidloggedAPI_2, SusConfig.COMPAT.fixInworldPreview);
        COMPAT.add("variousgrsissue", SusMods.of(GroovyScript).or(SusMods.of(CraftTweaker)), SusConfig.COMPAT.fixGrS);
        COMPAT.add("grsinlineicon", SusMods.of(GroovyScript), SusConfig.COMPAT.inlineIcon);

        BUGFIX.add("clipboardlighting", SusConfig.BUGFIX.clipboardLighting);
        BUGFIX.add("facadelighting", SusConfig.BUGFIX.facadeLighting);
        BUGFIX.add("implgetitem", SusConfig.BUGFIX.implGetItem);
        BUGFIX.add("packetdatamemleak", SusConfig.BUGFIX.packetMemLeak);
        BUGFIX.add("pipedatatransfer", SusConfig.BUGFIX.pipeDataTransfer);
        BUGFIX.add("pipeinvcrash", SusConfig.BUGFIX.pipeInvCrash);
        BUGFIX.add("invalidregistration", SusConfig.BUGFIX.invalidRegistration);
        BUGFIX.add("weakneighborref", SusConfig.BUGFIX.weakNeighborRef);

        TWEAK.add("tabnosearchbars", SusConfig.TWEAKS.noSearchBars);
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
                    supplier = supplier.and(() -> bool);
                } else {
                    throw new IllegalArgumentException("Invalid condition type: " + condition.getClass());
                }
            }
            MIXIN_CONFIGS.put(ROOT + this + MIXINS + name + JSON, supplier);
        }
    }
}
