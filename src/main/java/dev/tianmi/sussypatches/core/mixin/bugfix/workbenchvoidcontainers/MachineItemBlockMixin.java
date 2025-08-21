package dev.tianmi.sussypatches.core.mixin.bugfix.workbenchvoidcontainers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import gregtech.api.block.machines.MachineItemBlock;

@Mixin(value = MachineItemBlock.class, remap = false)
public abstract class MachineItemBlockMixin {

    @ModifyReceiver(method = "getContainerItem",
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/item/ItemStack;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;"))
    private ItemStack useSingleStack(ItemStack itemStack, Capability<IFluidHandlerItem> capability, EnumFacing _null) {
        var singleStack = itemStack.copy();
        singleStack.setCount(1);
        return singleStack;
    }
}
