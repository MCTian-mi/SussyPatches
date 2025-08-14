package dev.tianmi.sussypatches.api.core.asm;

import java.util.function.Consumer;

import org.objectweb.asm.tree.ClassNode;

public interface IExplicitTransformer extends Consumer<ClassNode> {

    String targetClassName();

    void transform(ClassNode targetClass);

    default void accept(ClassNode targetClass) {
        transform(targetClass);
    }
}
