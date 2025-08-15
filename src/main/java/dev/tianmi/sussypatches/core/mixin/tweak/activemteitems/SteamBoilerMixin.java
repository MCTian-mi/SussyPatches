package dev.tianmi.sussypatches.core.mixin.tweak.activemteitems;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.steam.boiler.SteamBoiler;

@Mixin(value = SteamBoiler.class, remap = false)
public abstract class SteamBoilerMixin extends MetaTileEntity {

    // Dummy
    SteamBoilerMixin() {
        super(null);
    }

    @ModifyExpressionValue(method = "renderMetaTileEntity",
                           at = @At(value = "INVOKE",
                                    target = "Lgregtech/common/metatileentities/steam/boiler/SteamBoiler;isBurning()Z"))
    private boolean alwaysActiveForItemRendering(boolean original) {
        return original || this.renderContextStack != null;
    }
}
