package dev.tianmi.sussypatches.api.core.asm;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.*;

public interface IExplicitTransformer extends Consumer<ClassNode> {

    Logger LOGGER = LogManager.getLogger("SusASM");

    String targetClassName();

    void transform(ClassNode targetClass);

    default void accept(ClassNode targetClass) {
        transform(targetClass);
    }

    @ApiStatus.Internal
    default void success() {
        LOGGER.debug("Class {} successfully patched by {}!", this.targetClassName(), this.getClass().getSimpleName());
    }

    @ApiStatus.Internal
    default void failure() {
        throw new AssertionError(String.format("%s failed to apply!", this.getClass().getSimpleName()));
    }
}
