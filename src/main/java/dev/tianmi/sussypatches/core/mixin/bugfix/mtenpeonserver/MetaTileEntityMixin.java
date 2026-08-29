package dev.tianmi.sussypatches.core.mixin.bugfix.mtenpeonserver;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.SideCacheExtension;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.metatileentity.MetaTileEntity;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2748")
@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityMixin implements SideCacheExtension {

    @Unique
    @Getter(lazy = true, onMethod_ = {@Unique, @Override})
    private final boolean sus$client = SusUtil.isWorldClient(getWorld());

    @Shadow
    public abstract World getWorld();

    @Redirect(method = "update",
              at = @At(value = "FIELD",
                                    target = "Lnet/minecraft/world/World;isRemote:Z",
                                    opcode = Opcodes.GETFIELD,
                                    remap = true))
    private boolean checkWorldServer(World instance) {
        return this.isClient();
    }

    @ModifyReceiver(method = "update",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/server/MinecraftServer;getTickCounter()I",
                             remap = true))
    private MinecraftServer useLocalWorld(MinecraftServer ignored) {
        return getWorld().getMinecraftServer();
    }
}
