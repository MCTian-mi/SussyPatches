package dev.tianmi.sussypatches.core.asm.transformer;

import dev.tianmi.sussypatches.api.annotation.Transformer;
import gregtech.api.cover.CoverWithUI;

@Transformer(target = CoverWithUI.class)
public class CoverWithUITransformer extends InvokeInterfaceInsnTransformer {

    private static final String TARGET_METHOD_NAME = "openUI";
    private static final String TARGET_METHOD_DESC = "(Lnet/minecraft/entity/player/EntityPlayerMP;)V";

    private static final String HANDLER_METHOD_SUFFIX = "openMui";
    private static final String HANDLER_METHOD_DESC = "(Lnet/minecraft/entity/player/EntityPlayerMP;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
    // private static final String HANDLER_METHOD_DESC =
    // "(Lgregtech/api/cover/CoverUIFactory;Lgregtech/api/gui/IUIHolder;Lnet/minecraft/entity/player/EntityPlayerMP;Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)V";

    @Override
    public String targetClassName() {
        return "gregtech.api.cover.CoverWithUI";
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
