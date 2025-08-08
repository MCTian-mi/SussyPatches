package dev.tianmi.sussypatches.core.mixin.bugfix.mtenpeonserver;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.metatileentity.MetaTileEntity;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2748")
@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityMixin {

    @Shadow
    public abstract World getWorld();

    @ModifyExpressionValue(method = "update",
                           at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/world/World;isRemote:Z",
                                    opcode = Opcodes.GETFIELD,
                                    remap = true))
    private boolean checkWorldServer(boolean original) {
        return original || getWorld().getMinecraftServer() == null;
    }

    @ModifyReceiver(method = "update",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/server/MinecraftServer;getTickCounter()I",
                             remap = true))
    private MinecraftServer useLocalWorld(MinecraftServer ignored) {
        return getWorld().getMinecraftServer();
    }
}
