package dev.tianmi.sussypatches.core.asm.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.annotation.Transformer;
import dev.tianmi.sussypatches.api.core.asm.IExplicitTransformer;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2851")
@Transformer(target = MultiblockPreviewRenderer.class)
public class MBPRTransformer implements IExplicitTransformer {

    public static final String TARGET_METHOD_NAME = "renderControllerInList";
    public static final String TARGET_METHOD_DESC = "(Lgregtech/api/metatileentity/multiblock/MultiblockControllerBase;Lgregtech/api/pattern/MultiblockShapeInfo;I)V";

    @Override
    public String targetClassName() {
        return "gregtech.client.renderer.handler.MultiblockPreviewRenderer";
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
                    int ordinal = 0;
                    for (var insnNode : instructions.toArray()) {
                        if ( // spotless:off
                                insnNode instanceof JumpInsnNode &&
                                insnNode.getOpcode() == Opcodes.GOTO &&
                                ordinal++ == 1
                        ) { // spotless:on
                            instructions.remove(insnNode);
                            return;
                        }
                    }
                }
            }
        }
    }
}
