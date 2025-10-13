package dev.tianmi.sussypatches.core.asm.transformer;

import dev.tianmi.sussypatches.api.annotation.Transformer;
import gregtech.api.items.toolitem.IGTTool;

@Transformer(target = IGTTool.class)
public class IGTToolTransformer extends InvokeInterfaceInsnTransformer {

    private static final String TARGET_METHOD_NAME = "definition$getSubItems";
    private static final String TARGET_METHOD_DESC = "(Lnet/minecraft/util/NonNullList;)V";

    private static final String HANDLER_METHOD_SUFFIX = "addAllMaterialSubtypes";
    private static final String HANDLER_METHOD_DESC = "(Lnet/minecraft/util/NonNullList;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";

    @Override
    public String targetClassName() {
        return "gregtech.api.items.toolitem.IGTTool";
    }

    @Override
    public String targetMethodName() {
        return TARGET_METHOD_NAME;
    }

    @Override
    public String targetMethodDesc() {
        return TARGET_METHOD_DESC;
    }

    @Override
    public String handlerMethodSuffix() {
        return HANDLER_METHOD_SUFFIX;
    }

    @Override
    public String handlerMethodDesc() {
        return HANDLER_METHOD_DESC;
    }
}
