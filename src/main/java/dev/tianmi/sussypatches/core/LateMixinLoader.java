package dev.tianmi.sussypatches.core;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return List.of(
                "sussypatches/feature/connectedtextures/mixins.gregtech.json");
    }
}
