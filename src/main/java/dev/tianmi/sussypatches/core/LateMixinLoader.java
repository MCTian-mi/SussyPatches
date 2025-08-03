package dev.tianmi.sussypatches.core;

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

public class LateMixinLoader implements ILateMixinLoader {

    private static final String ROOT = Tags.MODID + "/";
    private static final String MIXINS = "mixins.";
    private static final String JSON = ".json";

    private static final Map<String, BoolSupplier> MIXIN_CONFIGS = new LinkedHashMap<>();

    static {
        add(Type.FEATURE, "connectedtextures", SusMods.of(CTM), () -> SusConfig.FEAT.CTM);
        add(Type.COMPAT, "loliasm.ondemandanimation", SusMods.LoliASM, () -> SusConfig.COMPAT.ON_DEMAND);
        add(Type.COMPAT, "alfheim.dummyworldcrash", SusMods.of(Alfheim));
        add(Type.COMPAT, "vintagefix.lampbakedmodel", SusMods.VintageFix);
    }

    private static void add(Type type, String name, BoolSupplier... conditions) {
        BoolSupplier supplier = BoolSupplier.TRUE;
        for (var condition : conditions) {
            supplier = supplier.and(condition);
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

    private enum Type {

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
