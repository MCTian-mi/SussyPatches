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

    private static final String ROOT = Tags.MODID + "/";
    private static final String MIXINS = "mixins.";
    private static final String JSON = ".json";

    private static final Map<String, BoolSupplier> MIXIN_CONFIGS = new LinkedHashMap<>();

    static {
        add(FEATURE, "connectedtextures", SusMods.of(CTM), SusConfig.FEAT.multiCTM);

        add(COMPAT, "ondemandanimation", SusMods.LoliASM, SusConfig.COMPAT.fixOnDemand);
        add(COMPAT, "dummyworldcrash", SusMods.of(Alfheim), SusConfig.COMPAT.fixDummyWorld);
        add(COMPAT, "lampbakedmodel", SusMods.VintageFix, SusMods.of(CTM), SusConfig.COMPAT.fixLampModel);
        add(COMPAT, "inworldpreviewcrash", SusMods.FluidloggedAPI_2, SusConfig.COMPAT.fixInworldPreview);
        add(COMPAT, "variousgrsissue", SusMods.of(GroovyScript).or(SusMods.of(CraftTweaker)), SusConfig.COMPAT.fixGrS);
        add(COMPAT, "grsinlineicon", SusMods.of(GroovyScript), SusConfig.COMPAT.inlineIcon);

        add(BUGFIX, "clipboardlighting", SusConfig.BUGFIX.clipboardLighting);
        add(BUGFIX, "facadelighting", SusConfig.BUGFIX.facadeLighting);
        add(BUGFIX, "implgetitem", SusConfig.BUGFIX.implGetItem);
        add(BUGFIX, "packetdatamemleak", SusConfig.BUGFIX.packetMemLeak);
        add(BUGFIX, "pipedatatransfer", SusConfig.BUGFIX.pipeDataTransfer);
        add(BUGFIX, "pipeinvcrash", SusConfig.BUGFIX.pipeInvCrash);
        add(BUGFIX, "invalidregistration", SusConfig.BUGFIX.invalidRegistration);
        add(BUGFIX, "weakneighborref", SusConfig.BUGFIX.weakNeighborRef);

        add(TWEAK, "tabnosearchbars", SusConfig.TWEAKS.noSearchBars);
    }

    private static void add(Type type, String name, Object... conditions) {
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
        MIXIN_CONFIGS.put(ROOT + type + MIXINS + name + JSON, supplier);
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
        ;

        @Override
        public String toString() {
            return name().toLowerCase() + "/";
        }
    }
}
