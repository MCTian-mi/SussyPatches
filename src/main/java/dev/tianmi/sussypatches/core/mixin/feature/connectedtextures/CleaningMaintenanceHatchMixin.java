package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCleaningMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

// I hate special cases...
@Mixin(value = MetaTileEntityCleaningMaintenanceHatch.class, remap = false)
public abstract class CleaningMaintenanceHatchMixin extends MetaTileEntityMultiblockPart {

    // Dummy
    CleaningMaintenanceHatchMixin() {
        super(null, 0);
    }

    @WrapOperation(method = "renderMetaTileEntity",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/common/metatileentities/multi/multiblockpart/MetaTileEntityCleaningMaintenanceHatch;getBaseTexture()Lgregtech/client/renderer/ICubeRenderer;"))
    private ICubeRenderer overridesBaseTexture(MetaTileEntityCleaningMaintenanceHatch self,
                                               Operation<ICubeRenderer> method) {
        var controller = getController();
        if (controller != null) {
            ICubeRenderer renderer = ConnectedTextures.get(controller.metaTileEntityId, this);
            if (renderer != null) {
                return renderer;
            }
        }
        return method.call(self);
    }
}
