package dev.tianmi.sussypatches.api.core;

import static dev.tianmi.sussypatches.api.core.SusMixinTransformer.Type.TRANSFORMER;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import dev.tianmi.sussypatches.api.core.asm.IExplicitTransformer;
import dev.tianmi.sussypatches.api.core.mixin.extra.ISusMixin;
import dev.tianmi.sussypatches.core.asm.transformer.MBPRTransformer;

public class SusMixinTransformer {

    @SuppressWarnings("UnstableApiUsage")
    private static final Multimap<String, IExplicitTransformer> TRANSFORMERS = MultimapBuilder.hashKeys()
            .arrayListValues()
            .build();

    private static final List<ISusMixin> MIXINS = new ArrayList<>();

    static {
        TRANSFORMER.add(new MBPRTransformer());
    }

    public static void preApply(String targetClassName, ClassNode targetClass,
                                String mixinClassName, IMixinInfo mixinInfo) {
        TRANSFORMERS.get(targetClassName).forEach(transformer -> transformer.transform(targetClass));
        MIXINS.forEach(mixin -> mixin.applyPre(targetClass, mixinInfo));
    }

    public static void postApply(String targetClassName, ClassNode targetClass,
                                 String mixinClassName, IMixinInfo mixinInfo) {
        MIXINS.forEach(mixin -> mixin.applyPost(targetClass, mixinInfo));
    }

    enum Type {

        TRANSFORMER {

            @Override
            public void add(IExplicitTransformer transformer) {
                TRANSFORMERS.put(transformer.targetClassName(), transformer);
            }
        },
        MIXIN {

            @Override
            public void add(ISusMixin mixin) {
                MIXINS.add(mixin);
            }
        };

        void add(IExplicitTransformer transformer) {
            throw new UnsupportedOperationException();
        }

        void add(ISusMixin mixin) {
            throw new UnsupportedOperationException();
        }
    }
}
