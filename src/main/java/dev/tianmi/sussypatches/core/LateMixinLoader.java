package dev.tianmi.sussypatches.core;

import java.util.Arrays;
import java.util.List;

import zone.rong.mixinbooter.ILateMixinLoader;

public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList(
                "sussypatches/feature/connectedtextures/mixins.gregtech.json");
    }
}
