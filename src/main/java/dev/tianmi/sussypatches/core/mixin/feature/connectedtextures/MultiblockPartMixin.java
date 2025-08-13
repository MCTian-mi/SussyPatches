package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tianmi.sussypatches.api.core.mixin.extension.ConnectableExtension;
import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

@Mixin(value = MetaTileEntityMultiblockPart.class, remap = false)
public abstract class MultiblockPartMixin extends MetaTileEntity implements IMultiblockPart, ConnectableExtension {

    // Dummy
    MultiblockPartMixin() {
        super(null);
    }

    @Shadow
    public abstract MultiblockControllerBase getController();

    @Shadow
    public abstract ICubeRenderer getBaseTexture();

    @Unique
    @Nullable
    @Override
    public IBlockState sus$getVisualState(@Nullable IMultiblockPart part) {
        var controller = getController();
        if (getBaseTexture() instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.getVisualState();
        } else if (controller != null &&
                ConnectedTextures.get(controller.metaTileEntityId, this) instanceof VisualStateRenderer stateRenderer) {
                    return stateRenderer.getVisualState();
                }
        return null;
    }

    @Unique
    @Override
    public boolean canRenderInLayer(@NotNull BlockRenderLayer layer) {
        if (super.canRenderInLayer(layer)) {
            return true;
        } else if (getBaseTexture() instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.canRenderInLayer(layer);
        } else {
            var controller = getController();
            if (controller != null && ConnectedTextures.get(controller.metaTileEntityId,
                    this) instanceof VisualStateRenderer stateRenderer) {
                return stateRenderer.canRenderInLayer(layer);
            }
        }
        return false;
    }

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
