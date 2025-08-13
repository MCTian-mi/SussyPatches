package dev.tianmi.sussypatches.api.util;

import java.util.function.Function;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import org.jetbrains.annotations.NotNull;

import gregtech.api.util.Mods;

/// Mods that are :sus:
/// Basically a re-implementation of [Mods]
public enum SusMods implements BoolSupplier {

    GCyM(Names.GCYM),
    LoliASM(Names.LOLIASM),
    VintageFix(Names.VINTAGE_FIX),
    Lwjgl3ify(Names.LWJGL3IFY),
    ConfigAnytime(Names.CONFIGANYTIME),
    Cleanroom(Names.CLEANROOM),
    FluidloggedAPI_2(Names.FLUIDLOGGED_API, self -> self.version().startsWith("2")),
    FluidloggedAPI_3(Names.FLUIDLOGGED_API, self -> self.version().startsWith("3")),
    Celeritas(Names.CELERITAS),
    ModularUI(Names.MODULARUI),
    OpenGL3(null) { // Well true this isn't a mod, technically...

        @Override
        public boolean isLoaded() {
            return Cleanroom.isLoaded() || Lwjgl3ify.isLoaded();
        }
    };

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

    public static class Names {

        public static final String GCYM = "gcym";
        public static final String LOLIASM = "loliasm";
        public static final String VINTAGE_FIX = "vintagefix";
        public static final String LWJGL3IFY = "lwjgl3ify";
        public static final String CONFIGANYTIME = "configanytime";
        public static final String FLUIDLOGGED_API = "fluidlogged_api";
        public static final String CELERITAS = "celeritas";
        public static final String MODULARUI = "modularui";
        public static final String CLEANROOM = "cleanroom";
    }
}
