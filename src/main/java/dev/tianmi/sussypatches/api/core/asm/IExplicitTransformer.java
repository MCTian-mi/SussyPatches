package dev.tianmi.sussypatches.api.core.asm;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;

public interface IExplicitTransformer extends Consumer<ClassNode> {

    Logger LOGGER = LogManager.getLogger("SusASM");

    String targetClassName();

    void transform(ClassNode targetClass);

    default void accept(ClassNode targetClass) {
        transform(targetClass);
    }

    default void success() {
        LOGGER.debug("Class {} successfully patched by {}!", this.targetClassName(), this.getClass().getSimpleName());
    }

    default void failure() {
        throw new AssertionError(String.format("%s failed to apply!", this.getClass().getSimpleName()));
    }
}
