package dev.tianmi.sussypatches.core.mixin.bugfix.fasttransferdesync;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.util.PerTickIntCounter;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ModularUIContainer.class, remap = false)
public abstract class ModularUIContainerMixin {

    @Unique
    private int sus$quickMoveBudget;

    @WrapMethod(method = "slotClick", remap = true)
    private ItemStack sus$scopeQuickMoveBudget(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player,
                                               Operation<ItemStack> method) {
        if (clickTypeIn != ClickType.QUICK_MOVE) {
            return method.call(slotId, dragType, clickTypeIn, player);
        }
        int previousBudget = sus$quickMoveBudget;
        this.sus$quickMoveBudget = 0;
        try {
            return method.call(slotId, dragType, clickTypeIn, player);
        } finally {
            this.sus$quickMoveBudget = previousBudget;
        }
    }

    @WrapOperation(method = "transferStackInSlot", remap = true,
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/api/util/PerTickIntCounter;get(Lnet/minecraft/world/World;)I",
                            remap = false))
    private int sus$readQuickMoveBudget(PerTickIntCounter counter, World world, Operation<Integer> method) {
        return sus$quickMoveBudget;
    }

    @WrapOperation(method = "transferStackInSlot", remap = true,
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/api/util/PerTickIntCounter;increment(Lnet/minecraft/world/World;I)V",
                            remap = false))
    private void sus$addQuickMoveBudget(PerTickIntCounter counter, World world, int value, Operation<Void> method) {
        this.sus$quickMoveBudget += value;
    }

    @WrapOperation(method = "slotClick", remap = true,
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/inventory/Container;slotClick(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;",
                            ordinal = 0, remap = true))
    private ItemStack sus$reportQuickMove(ModularUIContainer container, int slotId, int dragType,
                                          ClickType clickTypeIn, EntityPlayer player, Operation<ItemStack> method) {
        if (clickTypeIn != ClickType.QUICK_MOVE) {
            return method.call(container, slotId, dragType, clickTypeIn, player);
        }
        val source = container.getSlot(slotId).getStack().copy();
        val result = method.call(container, slotId, dragType, clickTypeIn, player);
        // Vanilla can return EMPTY even after a successful transfer empties the source.
        return sus$quickMoveBudget > 0 ? source : result;
    }
}
