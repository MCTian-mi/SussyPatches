package dev.tianmi.sussypatches.api.util;

import java.util.function.Function;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.jetbrains.annotations.NotNull;

import gregtech.api.util.Mods;

/// Mods that are :sus:
public enum SusMods implements BoolSupplier {

    FLUIDLOGGED_API_2("fluidlogged_api", self -> self.version().startsWith("2")),
    FLUIDLOGGED_API_3("fluidlogged_api", self -> self.version().startsWith("3")),
    ;

    private final String ID;
    private final Function<SusMods, Boolean> extraCheck;
    private Boolean loaded;

    SusMods(String id) {
        this(id, null);
    }

    SusMods(String id, Function<SusMods, Boolean> extraCheck) {
        this.ID = id;
        this.extraCheck = extraCheck;
    }

    public static BoolSupplier of(Mods mod) {
        return mod::isModLoaded;
    }

    public String id() {
        return this.ID;
    }

    public boolean isLoaded() {
        if (this.loaded == null) {
            this.loaded = Loader.isModLoaded(this.ID);
            if (this.loaded) {
                if (this.extraCheck != null && !this.extraCheck.apply(this)) {
                    this.loaded = false;
                }
            }
        }
        return this.loaded;
    }

    @Override
    public boolean get() {
        return isLoaded();
    }

    @NotNull
    public String version() {
        ModContainer container = Loader.instance().getIndexedModList().get(this.ID);
        if (container == null) return "";
        return container.getVersion();
    }
}
