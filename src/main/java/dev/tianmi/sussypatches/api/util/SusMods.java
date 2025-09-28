package dev.tianmi.sussypatches.api.util;

import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GLContext;

import gregtech.api.util.Mods;

/// Mods that are :sus:
/// Basically a re-implementation of [Mods]
@ParametersAreNonnullByDefault
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
    NomiLibs(Names.NOMI_LIBS),
    RFTools(Names.RFTOOLS),
    /// Unlike [Mods#Optifine], this doesn't care about whether you use shaders or not.
    OptiFine(self -> FMLClientHandler.instance().hasOptifine()),

    // Well true these aren't mods, technically...
    OpenGL3(self -> GLContext.getCapabilities().OpenGL30),
    DevEnv(self -> FMLLaunchHandler.isDeobfuscatedEnvironment()),
    /// Basically the same as [Mods#Optifine], but with a more accurate name.
    /// Use with this in mind: The value of this is checked only once and never changed afterward.
    ShadersMod(self -> Mods.Optifine.isModLoaded()),
    ;

    @Nullable
    private final String ID;
    @Nullable
    private final Function<SusMods, Boolean> extraCheck;
    @Nullable
    private Boolean loaded;

    SusMods(String id) {
        this(id, null);
    }

    SusMods(Function<SusMods, Boolean> check) {
        this(null, check);
    }

    SusMods(@Nullable String id, @Nullable Function<SusMods, Boolean> extraCheck) {
        this.ID = id;
        this.extraCheck = extraCheck;
    }

    public static BoolSupplier of(Mods mod) {
        return mod::isModLoaded;
    }

    public boolean isLoaded() {
        if (this.loaded == null) {
            this.loaded = this.ID == null || Loader.isModLoaded(this.ID);
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
        if (this.ID == null) return "";
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
        public static final String NOMI_LIBS = "nomilabs";
        public static final String RFTOOLS = "rftools";
    }
}
