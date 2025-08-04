package dev.tianmi.sussypatches.core;

import static dev.tianmi.sussypatches.core.LateMixinLoader.Type.*;
import static gregtech.api.util.Mods.Alfheim;
import static gregtech.api.util.Mods.CTM;

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
        add(FEATURE, "connectedtextures", SusMods.of(CTM), SusConfig.FEAT.CTM);

        add(COMPAT, "ondemandanimation", SusMods.LoliASM, SusConfig.COMPAT.FIX_ON_DEMAND);
        add(COMPAT, "dummyworldcrash", SusMods.of(Alfheim), SusConfig.COMPAT.FIX_DUMMYWORLD);
        add(COMPAT, "lampbakedmodel", SusMods.VintageFix, SusConfig.COMPAT.FIX_LAMP_MODEL);
        add(COMPAT, "inworldpreviewcrash", SusMods.FluidloggedAPI_2, SusConfig.COMPAT.FIX_INWORLD_PREVIEW);

        add(BUGFIX, "clipboardlighting", SusConfig.BUGFIX.FIX_CLIPBOARD);
        add(BUGFIX, "facadelighting", SusConfig.BUGFIX.FIX_FACADE);
        add(BUGFIX, "variousgrsissue", SusConfig.BUGFIX.FIX_GRS);

        add(TWEAK, "tabnosearchbars", SusConfig.TWEAKS.NO_BARS);
    }

    private static void add(Type type, String name, Object... conditions) {
        BoolSupplier supplier = BoolSupplier.TRUE;
        for (var condition : conditions) {
            if (condition instanceof BoolSupplier boolSupplier) {
                supplier = supplier.and(boolSupplier);
            } else if (condition instanceof Boolean bool) {
                supplier.and(() -> bool);
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
