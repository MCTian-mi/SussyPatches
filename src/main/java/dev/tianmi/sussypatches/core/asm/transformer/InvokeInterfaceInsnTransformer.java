package dev.tianmi.sussypatches.core.asm.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.asm.IExplicitTransformer;

/// see [FabricMC/Mixin#181](https://github.com/FabricMC/Mixin/issues/181)
@Implemented(in = "https://github.com/FabricMC/Mixin/pull/182")
public abstract class InvokeInterfaceInsnTransformer implements IExplicitTransformer {

    public abstract String targetMethodName();

    public abstract String targetMethodDesc();

    public abstract String handlerMethodSuffix();

    public abstract String handlerMethodDesc();

    @Override
    public void transform(ClassNode classNode) {
        for (var methodNode : classNode.methods) {
            if ( // spotless:off
                    methodNode.name.equals(targetMethodName()) &&
                    methodNode.desc.equals(targetMethodDesc())
            ) { // spotless:on
                InsnList instructions = methodNode.instructions;
                if (instructions != null) {
                    for (var insnNode : instructions.toArray()) {
                        if ( // spotless:off
                                insnNode.getOpcode() == Opcodes.INVOKEINTERFACE &&
                                insnNode instanceof MethodInsnNode methodInsnNode &&
                                methodInsnNode.name.endsWith(handlerMethodSuffix()) &&
                                methodInsnNode.desc.equals(handlerMethodDesc())
                        ) { // spotless:on
                            methodInsnNode.setOpcode(Opcodes.INVOKESPECIAL);
                            success();
                            return;
                        }
                    }
                }
            }
        }
        error(); // Assume the bug is fixed w/ current mixin provider
    }
}
