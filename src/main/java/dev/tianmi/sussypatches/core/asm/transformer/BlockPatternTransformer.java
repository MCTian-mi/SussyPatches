package dev.tianmi.sussypatches.core.asm.transformer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import dev.tianmi.sussypatches.api.annotation.Transformer;
import dev.tianmi.sussypatches.api.core.asm.IExplicitTransformer;
import gregtech.api.pattern.BlockPattern;

@Transformer(target = BlockPattern.class)
public class BlockPatternTransformer implements IExplicitTransformer {

    private static final String TARGET_METHOD_NAME = "autoBuild";
    private static final String TARGET_METHOD_DESC = "(Lnet/minecraft/entity/player/EntityPlayer;Lgregtech/api/metatileentity/multiblock/MultiblockControllerBase;)V";

    private static final String FIND_LOCAL_NAME = "find";
    private static final String FIND_LOCAL_DESC = "Z";

    private static final String HAS_NEXT_OWNER = "java/util/Iterator";
    private static final String HAS_NEXT_NAME = "hasNext";
    private static final String HAS_NEXT_DESC = "()Z";

    @Override
    public String targetClassName() {
        return "gregtech.api.pattern.BlockPattern";
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

                    int localIndexFind = -1;

                    for (var localVariable : methodNode.localVariables) {
                        if ( // spotless:off
                                localVariable.name.equals(FIND_LOCAL_NAME) &&
                                localVariable.desc.equals(FIND_LOCAL_DESC)
                        ) { // spotless:on
                            localIndexFind = localVariable.index;
                            break;
                        }
                    }

                    JumpInsnNode outerJumpNode = null, limitedJumpNode = null, commonJumpNode = null;
                    LabelNode startLabel = null, middleLabel = null, endLabel = null;

                    int ordinalFindLoad = 0, ordinalHasNext = 0;
                    boolean startVisited = false, limitedVisited = false, commonVisited = false;

                    for (var insnNode : instructions.toArray()) {
                        if ( // spotless:off
                                insnNode.getOpcode() == Opcodes.ILOAD &&
                                insnNode instanceof VarInsnNode varInsnNode &&
                                varInsnNode.var == localIndexFind &&
                                varInsnNode.getNext() instanceof JumpInsnNode jumpInsnNode &&
                                jumpInsnNode.getOpcode() == Opcodes.IFNE &&
                                ordinalFindLoad++ == 1
                        ) { // spotless:on
                            outerJumpNode = jumpInsnNode;
                            endLabel = jumpInsnNode.label;
                            startLabel = new LabelNode();
                            startVisited = true;
                        } else if ( // spotless:off
                                insnNode.getOpcode() == Opcodes.INVOKEINTERFACE &&
                                insnNode instanceof MethodInsnNode methodInsnNode &&
                                methodInsnNode.owner.equals(HAS_NEXT_OWNER) &&
                                methodInsnNode.name.equals(HAS_NEXT_NAME) &&
                                methodInsnNode.desc.equals(HAS_NEXT_DESC) &&
                                methodInsnNode.getNext() instanceof JumpInsnNode jumpInsnNode
                        ) { // spotless:on
                                    if (ordinalHasNext == 3) {
                                        limitedJumpNode = jumpInsnNode;
                                        middleLabel = jumpInsnNode.label;
                                        limitedVisited = true;
                                    } else if (ordinalHasNext == 4) {
                                        commonJumpNode = jumpInsnNode;
                                        commonVisited = true;
                                    }
                                    ordinalHasNext++;
                                }

                        if (startVisited && limitedVisited && commonVisited) {

                            instructions.insert(outerJumpNode, startLabel);
                            instructions.insert(outerJumpNode, new JumpInsnNode(Opcodes.GOTO, middleLabel));
                            commonJumpNode.label = startLabel;
                            limitedJumpNode.label = endLabel;

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
