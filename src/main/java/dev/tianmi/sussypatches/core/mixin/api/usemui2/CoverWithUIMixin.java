package dev.tianmi.sussypatches.core.mixin.api.usemui2;

import net.minecraft.entity.player.EntityPlayerMP;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.SidedPosGuiData;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.annotation.Transformer;
import dev.tianmi.sussypatches.api.core.mixin.extension.mui2.CoverExtension;
import dev.tianmi.sussypatches.api.mui2.factory.CoverGuiFactory;
import dev.tianmi.sussypatches.core.asm.transformer.CoverWithUITransformer;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverWithUI;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2264")
@Transformer(clazz = CoverWithUITransformer.class)
@Mixin(value = CoverWithUI.class, remap = false)
public interface CoverWithUIMixin extends CoverExtension {

    /*
     * spotless:off
     *
     * This could not work because the bridge method seems to be injected even later than IMixinConfigPlugin#postApply.
     * Thus, the INVOKEINTERFACE insn cannot be transformed.
     *
     *  @WrapOperation(method = "openUI",
     *                 at = @At(value = "INVOKE",
     *                          target = "Lgregtech/api/cover/CoverUIFactory;openUI(Lgregtech/api/gui/IUIHolder;Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
     *  default void openMui(CoverUIFactory mui0Factory, IUIHolder cover, EntityPlayerMP player, Operation<Void> method) {
     *      if (!useMui2()) {
     *          method.call(mui0Factory, cover, player);
     *      } else {
     *          CoverGuiFactory.open(player, (Cover & IGuiHolder<SidedPosGuiData>) cover);
     *      }
     *  }
     * spotless:on
     */

    @Inject(method = "openUI", at = @At(value = "HEAD"), cancellable = true)
    default void openMui(EntityPlayerMP player, CallbackInfo ci) {
        if (!useMui2()) return;
        CoverGuiFactory.open(player, (Cover & IGuiHolder<SidedPosGuiData>) this);
        ci.cancel();
    }
}
