package dev.tianmi.sussypatches.api.util;

import gregtech.api.util.Mods;
import net.minecraftforge.fml.common.Loader;

/// Mods that are :sus:
public enum SusMods implements BoolSupplier {
    ;

    private final String ID;
    private final String NAME;
    private Boolean loaded;

    SusMods(String id, String name) {
        this.ID = id;
        this.NAME = name;
    }

    public static BoolSupplier of(Mods mod) {
        return mod::isModLoaded;
    }

    public String getId() {
        return this.ID;
    }

    public String getName() {
        return this.NAME;
    }

    public boolean isLoaded() {
        if (this.loaded == null) {
            this.loaded = Loader.isModLoaded(this.ID);
        }
        return this.loaded;
    }

    @Override
    public boolean get() {
        return isLoaded();
    }
}
