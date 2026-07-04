package dev.tianmi.sussypatches.core;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.core.ILoadingPlugin;
import dev.tianmi.sussypatches.api.util.BoolSupplier;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.tianmi.sussypatches.core.LoadingPlugin.Type.API;
import static dev.tianmi.sussypatches.core.LoadingPlugin.Type.COMPAT;

@Name("SussyPatchesPlugin")
@MCVersion(ForgeVersion.mcVersion)
@TransformerExclusions("dev.tianmi.sussypatches.core.asm.")
public class LoadingPlugin implements ILoadingPlugin, IEarlyMixinLoader {

    private static final Map<String, BoolSupplier> MIXIN_CONFIGS = new LinkedHashMap<>();

    static {
        // TODO: fix early config
        API.add("itemoverlayevent"/* , SusConfig.API.itemOverlayEvent */);

        COMPAT.add("realtimeshadercheck"/* , SusConfig.COMPAT.realTimeShaderCheck, SusMods.OptiFine */);
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

        private static final String ROOT = Tags.MOD_ID + "/";
        private static final String MIXINS = "mixins.";
        private static final String JSON = ".json";

        @Override
        public String toString() {
            return name().toLowerCase() + "/";
        }

        public void add(String name, Object... conditions) {
            MIXIN_CONFIGS.put(ROOT + this + MIXINS + name + JSON, BoolSupplier.concat(conditions));
        }
    }
}
