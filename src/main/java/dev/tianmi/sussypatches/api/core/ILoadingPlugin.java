package dev.tianmi.sussypatches.api.core;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import org.jetbrains.annotations.Nullable;

/// A utility interface giving the default impl for [IFMLLoadingPlugin].
public interface ILoadingPlugin extends IFMLLoadingPlugin {

    /// @return an array of classes that implements the IClassTransformer interface
    @Override
    default String[] getASMTransformerClass() {
        return new String[0];
    }

    /// Return a class name that implements "ModContainer" for injection into the mod list
    /// The "getName" function should return a name that other mods can, if one need to be,
    /// depend on.
    /// Trivially, this mod container will be loaded before all regular mod containers,
    /// which means it will be forced to be "immutable" - not susceptible to normal
    /// sorting behavior.
    /// All other mod behaviors are available however - this container can receive and handle
    /// normal loading events
    @Override
    default String getModContainerClass() {
        return null;
    }

    /// Return the class name of an implementor of "IFMLCallHook", that will be run, in the
    /// main thread, to perform any additional setup this coremod may require. It will be
    /// run **prior** to Minecraft starting, so it CANNOT operate on minecraft
    /// itself. The game will deliberately crash if this code is detected to trigger a
    /// minecraft class loading
    /// TODO)) implement crash ;)
    @Nullable
    @Override
    default String getSetupClass() {
        return null;
    }

    /// Inject coremod data into this coremod
    /// This data includes:
    /// "mcLocation": the location of the minecraft directory,
    /// "coremodList": the list of coremods
    /// "coremodLocation": the file this coremod loaded from.
    @Override
    default void injectData(Map<String, Object> data) {
        /* Do nothing */
    }

    /// Return an optional access transformer class for this coremod. It will be injected post-deobf
    /// so ensure your ATs conform to the new srgnames scheme.
    ///
    /// @return the name of an access transformer class or null if none is provided
    @Override
    default String getAccessTransformerClass() {
        return null;
    }
}
