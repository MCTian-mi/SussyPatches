package dev.tianmi.sussypatches.core.mixin.compat.nopipeforscanner;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.annotation.Compat;
import dev.tianmi.sussypatches.api.util.SusMods;
import gregtech.api.pipenet.tile.IPipeTile;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;

@Compat(mods = SusMods.RFTools)
@Mixin(value = StorageScannerTileEntity.class, remap = false)
@SuppressWarnings("UnresolvedMixinReference")
public class StorageScannerTileEntityMixin {

    @ModifyExpressionValue(method = "findInventories",
                           at = @At(value = "INVOKE",
                                    target = "Lmcjty/lib/container/InventoryHelper;isInventory(Lnet/minecraft/tileentity/TileEntity;)Z"))
    private boolean noPipes(boolean original, @Local(name = "te") TileEntity te) {
        return original && !(te instanceof IPipeTile<?, ?>);
    }

    @ModifyExpressionValue(method = "inventoryAddNew",
                           at = @At(value = "INVOKE",
                                    target = "Lmcjty/lib/container/InventoryHelper;isInventory(Lnet/minecraft/tileentity/TileEntity;)Z"))
    private boolean noPipesToo(boolean original, @Local(name = "te") TileEntity te) {
        return original && !(te instanceof IPipeTile<?, ?>);
    }
}
