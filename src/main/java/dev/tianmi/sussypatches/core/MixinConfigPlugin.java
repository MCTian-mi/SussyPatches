package dev.tianmi.sussypatches.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import dev.tianmi.sussypatches.api.core.IMixinConfigPlugin;
import dev.tianmi.sussypatches.core.asm.SusTransformers;

public class MixinConfigPlugin implements IMixinConfigPlugin {

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        SusTransformers.transform(targetClassName, targetClass);
    }
}
