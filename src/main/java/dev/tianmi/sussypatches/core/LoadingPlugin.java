package dev.tianmi.sussypatches.core;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import dev.tianmi.sussypatches.api.core.ILoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

@Name("SussyPatchesPlugin")
@MCVersion(ForgeVersion.mcVersion)
@TransformerExclusions("dev.tianmi.sussypatches.core.")
public class LoadingPlugin implements ILoadingPlugin, IEarlyMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.emptyList();
    }
}
