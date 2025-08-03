package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tianmi.sussypatches.api.metatileentity.IConnectable;
import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = MetaTileEntityMultiblockPart.class, remap = false)
public abstract class MultiblockPartsMixin extends MetaTileEntity implements IMultiblockPart, IConnectable {

    // Dummy
    MultiblockPartsMixin() {
        super(null);
    }

    @Shadow
    public abstract MultiblockControllerBase getController();

    @WrapOperation(method = "renderMetaTileEntity",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/common/metatileentities/multi/multiblockpart/MetaTileEntityMultiblockPart;getBaseTexture()Lgregtech/client/renderer/ICubeRenderer;"))
    private ICubeRenderer overridesBaseTexture(MetaTileEntityMultiblockPart self, Operation<ICubeRenderer> method) {
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
