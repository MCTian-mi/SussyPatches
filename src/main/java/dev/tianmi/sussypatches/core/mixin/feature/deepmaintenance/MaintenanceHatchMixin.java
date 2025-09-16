package dev.tianmi.sussypatches.core.mixin.feature.deepmaintenance;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(SusUtil.class)
@Mixin(value = MetaTileEntityMaintenanceHatch.class, remap = false)
public class MaintenanceHatchMixin {

    // This is a hard rewrite, any conflict should result in a hard crash
    @Redirect(method = { "fixProblemsWithTools", "fixMaintenanceProblems" },
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/entity/player/InventoryPlayer;mainInventory:Lnet/minecraft/util/NonNullList;",
                       opcode = Opcodes.GETFIELD,
                       remap = true),
              require = 2)
    public NonNullList<ItemStack> gatherAllItems(InventoryPlayer inventoryPlayer) {
        return inventoryPlayer.player.gatherAllItems();
    }
}
