package dev.tianmi.sussypatches.api.core.mixin.extra;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public interface ISusMixin {

    default void applyPre(ClassNode classNode, IMixinInfo mixinInfo) {
        /* Do nothing */
    }

    default void applyPost(ClassNode classNode, IMixinInfo mixinInfo) {
        /* Do nothing */
    }
}
