package dev.tianmi.sussypatches.core;

import dev.tianmi.sussypatches.api.core.IMixinConfigPlugin;
import dev.tianmi.sussypatches.core.asm.SusTransformers;
import lombok.val;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        SusTransformers.transform(targetClassName, targetClass);

        // Stops jvmdg annotations from being merged
        val annotations = targetClass.visibleAnnotations;
        if (annotations != null) {
            annotations.removeIf(it -> it.desc.contains("xyz/wagyourtail/jvmdg"));
        }
    }
}
