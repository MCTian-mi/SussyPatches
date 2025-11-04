package dev.tianmi.sussypatches.core.mixin.bugfix.weakneighborref;

import static dev.tianmi.sussypatches.api.core.mixin.extension.NeighborCacheExtension.isAdjacentChunkUnloaded;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.NeighborCacheExtension;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.metatileentity.interfaces.INeighborCache;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2828")
@Mixin(value = NeighborCacheTileEntityBase.class, remap = false)
public abstract class NeighborCacheTileEntityBaseMixin extends SyncedTileEntityBase
                                                       implements NeighborCacheExtension, INeighborCache {

    @Mutable
    @Final
    @Shadow
    private TileEntity[] neighbors;

    @Shadow
    private boolean neighborsInvalidated;

    @Unique
    private final List<WeakReference<TileEntity>> sus$neighbors = Arrays.asList(INVALID, INVALID, INVALID, INVALID,
            INVALID, INVALID);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void clearOriginalImpl(CallbackInfo ci) {
        this.neighbors = null;
    }

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = "invalidateNeighbors",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Arrays;fill([Ljava/lang/Object;Ljava/lang/Object;)V"))
    private void invalidateWeakRefs(Object[] _null, Object _this) {
        for (var facing : EnumFacing.values()) {
            this.sus$neighbors.set(facing.getIndex(), INVALID);
        }
    }

    /**
     * @author Ghzdude, Tian_mi
     * @reason This is a hard rewrite, any conflict should result in a hard crash
     */
    @Nullable
    @Override
    @Overwrite
    public TileEntity getNeighbor(@NotNull EnumFacing facing) {
        if (world == null || pos == null) return null;
        // if the ref is INVALID, compute neighbor, otherwise, return TE or null
        var ref = sus$invalidRef(facing) ? sus$computeNeighbor(facing) : sus$getRef(facing);
        return ref.get();
    }

    /**
     * @author Ghzdude, Tian_mi
     * @reason This is a hard rewrite, any conflict should result in a hard crash
     */
    @Overwrite
    public void onNeighborChanged(@NotNull EnumFacing facing) {
        this.sus$neighbors.set(facing.getIndex(), INVALID);
    }

    @Unique
    @NotNull
    @Override
    public WeakReference<TileEntity> sus$computeNeighbor(EnumFacing facing) {
        TileEntity te = super.getNeighbor(facing);
        // avoid making new references to null TEs
        var ref = te == null ? NULL : new WeakReference<>(te);
        this.sus$neighbors.set(facing.getIndex(), ref);
        this.neighborsInvalidated = false;
        return ref;
    }

    @Unique
    @NotNull
    @Override
    public WeakReference<TileEntity> sus$getRef(EnumFacing facing) {
        return this.sus$neighbors.get(facing.getIndex());
    }

    @Unique
    @Override
    public boolean sus$invalidRef(EnumFacing facing) {
        WeakReference<TileEntity> ref = sus$getRef(facing);
        if (ref == INVALID) return true;
        TileEntity te = ref.get();
        if (te == null && isAdjacentChunkUnloaded(world, pos, facing)) {
            return true;
        }
        return te != null && te.isInvalid();
    }
}
