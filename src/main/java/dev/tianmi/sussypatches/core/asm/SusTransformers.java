package dev.tianmi.sussypatches.core.asm;

import org.objectweb.asm.tree.ClassNode;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import dev.tianmi.sussypatches.api.core.asm.IExplicitTransformer;
import dev.tianmi.sussypatches.core.asm.transformer.BlockPatternTransformer;
import dev.tianmi.sussypatches.core.asm.transformer.IGTToolTransformer;
import dev.tianmi.sussypatches.core.asm.transformer.MBPRTransformer;

public class SusTransformers {

    @SuppressWarnings("UnstableApiUsage")
    private static final Multimap<String, IExplicitTransformer> TRANSFORMERS = MultimapBuilder.hashKeys()
            .arrayListValues()
            .build();

    static {
        add(new MBPRTransformer());
        add(new IGTToolTransformer());
        add(new BlockPatternTransformer());
    }

    public static void transform(String targetClassName, ClassNode targetClass) {
        TRANSFORMERS.get(targetClassName).forEach(transformer -> transformer.transform(targetClass));
    }

    private static void add(IExplicitTransformer transformer) {
        TRANSFORMERS.put(transformer.targetClassName(), transformer);
    }
}
