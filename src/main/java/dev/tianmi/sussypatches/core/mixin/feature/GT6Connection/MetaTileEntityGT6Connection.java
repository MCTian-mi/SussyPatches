package dev.tianmi.sussypatches.core.mixin.feature.GT6Connection;

import java.util.Set;
import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import codechicken.lib.raytracer.CuboidRayTraceResult;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.cover.CoverRayTracer;

@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityGT6Connection {

    @Shadow public abstract net.minecraft.world.World getWorld();
    @Shadow public abstract net.minecraft.util.math.BlockPos getPos();

    @Inject(method = "onToolClick", at = @At("HEAD"), cancellable = true)
    private void relayToolClick(EntityPlayer playerIn, Set<String> toolClasses, EnumHand hand,
                                CuboidRayTraceResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        // Only work when sneaking
        if (playerIn == null || !playerIn.isSneaking()) return;

        // Fast exit (Relay click do not apply on pipe/cable)
        String thisName = this.getClass().getSimpleName();
        if (!toolClasses.contains("screwdriver")) {
            if (thisName.equals("TileEntityCable") || thisName.equals("TileEntityFluidPipeTickable") || thisName.equals("TileEntityItemStackPipeTickable")) {
                return; 
            }
        }

        // # Grid side detection
        EnumFacing gridSideHit = CoverRayTracer.determineGridSideHit(hitResult);
        if (gridSideHit == null) return;

        // Locate Neighbour
        BlockPos neighbourPos = getPos().offset(gridSideHit);
        TileEntity te = getWorld().getTileEntity(neighbourPos);
        if (te == null) return;

        String neighbourName = te.getClass().getSimpleName();
        EnumFacing neighbourSide = gridSideHit.getOpposite();
        boolean handled = false;

        // --- Cover Relay (Screwdriver) ---
        if (toolClasses.contains("screwdriver")) {
            Object holder = te.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER, neighbourSide);
            if (holder != null) {
                try {
                    Cover cover = null;
                    if (holder instanceof MetaTileEntity) {
                        cover = ((MetaTileEntity) holder).getCoverAtSide(neighbourSide);
                    } else {
                        Method getCover = holder.getClass().getMethod("getCoverAtSide", EnumFacing.class);
                        cover = (Cover) getCover.invoke(holder, neighbourSide);
                    }
                    if (cover != null) {
                        handled = cover.onScrewdriverClick(playerIn, hand, hitResult) == EnumActionResult.SUCCESS;
                    }
                } catch (Exception ignored) {}
            }
        }

        // --- Pipe Relay (Wrench) ---
        if (!handled && toolClasses.contains("wrench")) {
            // Only relaying wrench to pipes
            if (neighbourName.equals("TileEntityFluidPipeTickable") || neighbourName.equals("TileEntityItemStackPipeTickable")) {
                if (te instanceof IGregTechTileEntity) {
                    MetaTileEntity neighbourMTE = ((IGregTechTileEntity) te).getMetaTileEntity();
                    if (neighbourMTE != null) {
                        EnumFacing originalSide = hitResult.sideHit;
                        hitResult.sideHit = neighbourSide;
                        handled = neighbourMTE.onWrenchClick(playerIn, hand, neighbourSide, hitResult);
                        hitResult.sideHit = originalSide;
                    }
                } else if (te instanceof TileEntityPipeBase<?, ?>) {
                    handled = toggleConnection(te, neighbourSide, neighbourPos, playerIn, "Pipe");
                }
            }
        }

        // --- Wire/Cable Relay (Wirecutter) ---
        if (!handled && toolClasses.contains("wirecutter")) {
            // Avoid relaying Wirecutter to Fluid Pipes
            if (neighbourName.equals("TileEntityCable")) {
                if (te instanceof IGregTechTileEntity) {
                    MetaTileEntity neighbourMTE = ((IGregTechTileEntity) te).getMetaTileEntity();
                    if (neighbourMTE != null) {
                        EnumFacing originalSide = hitResult.sideHit;
                        hitResult.sideHit = neighbourSide;
                        // Use onToolClick for wirecutters to ensure logic triggers
                        handled = neighbourMTE.onToolClick(playerIn, toolClasses, hand, hitResult);
                        hitResult.sideHit = originalSide;
                    }
                } else if (te instanceof TileEntityPipeBase<?, ?>) {
                    handled = toggleConnection(te, neighbourSide, neighbourPos, playerIn, "Cable");
                }
            }
        }

        if (handled) {
            cir.setReturnValue(true);
        }
    }

    private boolean toggleConnection(TileEntity te, EnumFacing side, BlockPos pos, EntityPlayer player, String type) {
        TileEntityPipeBase<?, ?> pipe = (TileEntityPipeBase<?, ?>) te;
        boolean isConnected = pipe.isConnected(side);
        pipe.setConnection(side, !isConnected, true);
        getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(pos), getWorld().getBlockState(pos), 3);
        return true;
    }

    // Holding wirecutter and screwdriver in either hand force the # grid to render
    @Inject(method = "canRenderMachineGrid", at = @At("HEAD"), cancellable = true)
    private void forceToolGrid(ItemStack mainHandStack, ItemStack offHandStack, CallbackInfoReturnable<Boolean> cir) {
        if (isValidRelayTool(mainHandStack) || isValidRelayTool(offHandStack)) {
            cir.setReturnValue(true);
        }
    }

    private boolean isValidRelayTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Set<String> toolClasses = stack.getItem().getToolClasses(stack);
        
        return toolClasses.contains("wirecutter") || 
               toolClasses.contains("screwdriver");
    }
}