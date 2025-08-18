package dev.tianmi.sussypatches.core;

import static dev.tianmi.sussypatches.core.LoadingPlugin.Type.*;

import java.util.*;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.core.ILoadingPlugin;
import dev.tianmi.sussypatches.api.util.BoolSupplier;
import zone.rong.mixinbooter.IEarlyMixinLoader;

@Name("SussyPatchesPlugin")
@MCVersion(ForgeVersion.mcVersion)
@TransformerExclusions("dev.tianmi.sussypatches.core.asm.")
public class LoadingPlugin implements ILoadingPlugin, IEarlyMixinLoader {

    private static final Map<String, BoolSupplier> MIXIN_CONFIGS = new LinkedHashMap<>();

    static {
        // TODO: fix early config
        API.add("itemoverlayevent"/* , SusConfig.API.itemOverlayEvent */);
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
