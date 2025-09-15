package dev.tianmi.sussypatches.core.mixin.api.usemui2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.Mui2Extension;
import dev.tianmi.sussypatches.api.mui.factory.MTEGuiFactory;
import gregtech.api.metatileentity.MetaTileEntity;

@SuppressWarnings("deprecation")
@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2281")
@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityMixin implements Mui2Extension {

    @Shadow
    protected abstract boolean openGUIOnRightClick();

    @Shadow
    public abstract World getWorld();

    @Inject(method = "onRightClick", at = @At("HEAD"), cancellable = true)
    private void onRightClickMUI(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        if (useMui2() && !playerIn.isSneaking() && openGUIOnRightClick()) {
            if (getWorld() != null && !getWorld().isRemote) {
                MTEGuiFactory.open(playerIn, (MetaTileEntity & IGuiHolder<PosGuiData>) (Object) this);
            }
            cir.setReturnValue(true);
        }
    }
}
