package dev.tianmi.sussypatches.core.asm.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.annotation.Transformer;
import dev.tianmi.sussypatches.api.core.asm.IExplicitTransformer;
import gregtech.api.items.toolitem.IGTTool;

/// see [FabricMC/Mixin#181](https://github.com/FabricMC/Mixin/issues/181)
@Implemented(in = "https://github.com/FabricMC/Mixin/pull/182")
@Transformer(target = IGTTool.class)
public class IGTToolTransformer implements IExplicitTransformer {

    private static final String TARGET_METHOD_NAME = "definition$getSubItems";
    private static final String TARGET_METHOD_DESC = "(Lnet/minecraft/util/NonNullList;)V";

    private static final String HANDLER_METHOD_SUFFIX = "addAllMaterialSubtypes";
    private static final String HANDLER_METHOD_DESC = "(Lnet/minecraft/util/NonNullList;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";

    @Override
    public String targetClassName() {
        return "gregtech.api.items.toolitem.IGTTool";
    }

    @Override
    public void transform(ClassNode classNode) {
        for (var methodNode : classNode.methods) {
            if ( // spotless:off
                    methodNode.name.equals(TARGET_METHOD_NAME) &&
                    methodNode.desc.equals(TARGET_METHOD_DESC)
            ) { // spotless:on
                InsnList instructions = methodNode.instructions;
                if (instructions != null) {
                    for (var insnNode : instructions.toArray()) {
                        if ( // spotless:off
                                insnNode.getOpcode() == Opcodes.INVOKEINTERFACE &&
                                insnNode instanceof MethodInsnNode methodInsnNode &&
                                methodInsnNode.name.endsWith(HANDLER_METHOD_SUFFIX) &&
                                methodInsnNode.desc.equals(HANDLER_METHOD_DESC)
                        ) { // spotless:on
                            methodInsnNode.setOpcode(Opcodes.INVOKESPECIAL);
                            success();
                            return;
                        }
                    }
                }
            }
        }
        failure();
    }
}
