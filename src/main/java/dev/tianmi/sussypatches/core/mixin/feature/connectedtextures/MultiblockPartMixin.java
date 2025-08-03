package dev.tianmi.sussypatches.core.mixin.feature.connectedtextures;

import dev.tianmi.sussypatches.api.metatileentity.IConnectable;
import dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures;
import dev.tianmi.sussypatches.client.renderer.textures.custom.VisualStateRenderer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MetaTileEntityMultiblockPart.class, remap = false)
public abstract class MultiblockPartMixin extends MetaTileEntity implements IMultiblockPart, IConnectable {

    // Dummy
    MultiblockPartMixin() {
        super(null);
    }

    @Shadow
    public abstract MultiblockControllerBase getController();

    @Shadow
    public abstract ICubeRenderer getBaseTexture();

    @Nullable
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public IBlockState getVisualState(@Nullable IMultiblockPart part) {
        var controller = getController();
        if (getBaseTexture() instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.getVisualState();
        } else if (controller != null &&
                ConnectedTextures.get(controller.metaTileEntityId, this) instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.getVisualState();
        }
        return null;
    }

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
}
