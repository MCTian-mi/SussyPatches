package dev.tianmi.sussypatches.core.mixin.tweak.betteritemfluidhandler;

import static dev.tianmi.sussypatches.api.capability.impl.SusFluidHandlerIS.LOCKED_FLUID_NBT_KEY;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.capability.impl.SusFluidHandlerIS;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;

@Mixin(value = MetaTileEntityQuantumTank.class, remap = false)
public abstract class QuantumTankMixin {

    @Redirect(method = "initItemStackCapabilities",
              at = @At(value = "NEW",
                       target = "gregtech/api/capability/impl/GTFluidHandlerItemStack"))
    private static GTFluidHandlerItemStack useSusFHIS(ItemStack container, int capacity) {
        return new SusFluidHandlerIS(container, capacity);
    }

    @SuppressWarnings("InvokeAssignCanReplacedWithExpression")
    @Inject(method = "addInformation",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lnet/minecraft/item/ItemStack;getTagCompound()Lnet/minecraft/nbt/NBTTagCompound;"))
    private void addLockedFluid(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced,
                                CallbackInfo ci, @Local(name = "tag") NBTTagCompound tag) {
        if (tag == null || !tag.hasKey(LOCKED_FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) return;
        var lockedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(LOCKED_FLUID_NBT_KEY));
        if (lockedFluid == null) return;
        tooltip.add(I18n.format("sussypatches.universal.tooltip.fluid_locked", lockedFluid.getLocalizedName()));
    }
}
