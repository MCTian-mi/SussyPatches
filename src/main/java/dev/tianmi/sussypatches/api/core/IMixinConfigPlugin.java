package dev.tianmi.sussypatches.api.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/// A utility interface giving the default impl
/// for [org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin].
public interface IMixinConfigPlugin extends org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin {

    /// Called after the plugin is instantiated, do any setup here.
    ///
    /// @param mixinPackage The mixin root package from the config
    @Override
    default void onLoad(String mixinPackage) {
        /* Do nothing */
    }

    /// Called only if the "referenceMap" key in the config is **not** set.
    /// This allows the refmap file name to be supplied by the plugin
    /// programmatically if desired. Returning <code>null</code> will revert to
    /// the default behavior of using the default refmap JSON file.
    ///
    /// @return Path to the refmap resource or null to revert to the default
    @Override
    default String getRefMapperConfig() {
        return "";
    }

    /// Called during mixin initialisation, allows this plugin to control whether
    /// a specific will be applied to the specified target. Returning false will
    /// remove the target from the mixin's target set, and if all targets are
    /// removed, then the mixin will not be applied at all.
    ///
    /// @param targetClassName Fully qualified class name of the target class
    /// @param mixinClassName Fully qualified class name of the mixin
    /// @return True to allow the mixin to be applied, or false to remove it from
    /// target's mixin set
    @Override
    default boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    /// Called after all configurations are initialized, this allows this plugin
    /// to observe classes targeted by other mixin configs and optionally remove
    /// targets from its own set. The set myTargets is a direct view of the
    /// target collection in this companion config and keys may be removed from
    /// this set to suppress mixins in this config which target the specified
    /// class. Adding keys to the set will have no effect.
    ///
    /// @param myTargets Target class set from the companion config
    /// @param otherTargets Target class set incorporating targets from all other
    /// configs, read-only
    @Override
    default void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        /* Do nothing */
    }

    /// After mixins specified in the configuration have been processed, this
    /// method is called to allow the plugin to add any additional mixins to
    /// load. It should return a list of mixin class names or return null if the
    /// plugin does not wish to append any mixins of its own.
    ///
    /// @return additional mixins to apply
    @Override
    default List<String> getMixins() {
        return Collections.emptyList();
    }

    /// Called immediately **before** a mixin is applied to a target class,
    /// allows any pre-application transformations to be applied.
    ///
    /// @param targetClassName Transformed name of the target class
    /// @param targetClass Target class tree
    /// @param mixinClassName Name of the mixin class
    /// @param mixinInfo Information about this mixin
    @Override
    default void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        /* Do nothing */
    }

    /// Called immediately **after** a mixin is applied to a target class,
    /// allows any post-application transformations to be applied.
    ///
    /// @param targetClassName Transformed name of the target class
    /// @param targetClass Target class tree
    /// @param mixinClassName Name of the mixin class
    /// @param mixinInfo Information about this mixin
    @Override
    default void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        /* Do nothing */
    }
}
