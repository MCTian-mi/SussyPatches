package dev.tianmi.sussypatches.core.mixin.compat.variousgrsissue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.cleanroommc.groovyscript.event.GsHandEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.unification.material.Material;
import gregtech.api.util.ClipboardUtil;
import gregtech.api.util.Mods;
import gregtech.integration.groovy.GroovyHandCommand;

@Compat(mod = Mods.GroovyScript)
@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2785")
@Mixin(value = GroovyHandCommand.class, remap = false)
public abstract class GroovyHandCommandMixin {

    @ModifyArg(method = "onHandCommand",
               at = @At(target = "Ljava/lang/StringBuilder;append(Ljava/lang/Object;)Ljava/lang/StringBuilder;",
                        value = "INVOKE",
                        ordinal = 0))
    private static Object toNameSpace(Object obj) {
        if (obj instanceof Material material) {
            return SusUtil.getPrefix(material) + material;
        } else throw new AssertionError("Unexpected type: " + obj.getClass());
    }

    // Using hard @Redirect here since it's a bug anyway.
    @Redirect(method = "onHandCommand",
              at = @At(value = "FIELD",
                       target = "Lcom/cleanroommc/groovyscript/event/GsHandEvent;player:Lnet/minecraft/entity/player/EntityPlayer;",
                       opcode = Opcodes.GETFIELD))
    private static EntityPlayer passBadAssertion(GsHandEvent event) {
        return null;
    }

    @WrapOperation(method = "onHandCommand",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/api/util/ClipboardUtil;copyToClipboard(Lnet/minecraft/entity/player/EntityPlayerMP;Ljava/lang/String;)V"))
    private static void safeCopyToClipboard(EntityPlayerMP _null, String text, Operation<Void> method,
                                            @Local(argsOnly = true) GsHandEvent event) {
        if (event.player instanceof EntityPlayerMP playerMP) {
            method.call(playerMP, text);
        } else if (FMLCommonHandler.instance().getSide().isClient()) {
            ClipboardUtil.copyToClipboard(text);
        }
    }
}
