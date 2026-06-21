package dev.tianmi.sussypatches.core.mixin.tweak.betteritemfluidhandler;

import static net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack.FLUID_NBT_KEY;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.capability.impl.SusFluidHandlerIS;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeTank;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;

@Mixin(value = MetaTileEntityCreativeTank.class, remap = false)
public abstract class CreativeTankMixin extends MetaTileEntityQuantumTank {

    // Dummy
    CreativeTankMixin() {
        super(null, 0, 0);
    }

    @Unique
    @Override
    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        // A custom SusFHIS that can supply/consume infinite fluid
        return new SusFluidHandlerIS(itemStack, Integer.MAX_VALUE) {

            @Nullable
            @Override
            public FluidStack getFluid() {
                var fluidStack = super.getFluid();
                if (fluidStack != null) fluidStack.amount = Integer.MAX_VALUE; // Supply infinite fluid
                return fluidStack;
            }

            @Nullable
            @Override
            protected FluidStack getLockedFluid() {
                return null; // No fluid locking for creative tanks
            }

            @Override
            public FluidStack drain(int maxDrain, boolean doDrain) {
                if (container.getCount() != 1 || maxDrain <= 0) return null;

                FluidStack contained = getFluid();
                if (contained == null || contained.amount <= 0 || !canDrainFluidType(contained)) return null;

                var drained = contained.copy();
                drained.amount = maxDrain; // Always drain as many as one want
                return drained;
            }

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                if (container.getCount() != 1 || resource == null || resource.amount <= 0 ||
                        !canFillFluidType(resource)) {
                    return 0;
                }

                FluidStack contained = getFluid();
                if (contained != null && contained.isFluidEqual(resource)) {
                    return resource.amount; // Consume all whatever
                }
                return 0; // Don't accept insertion when empty
            }
        };
    }

    @Inject(method = "addInformation", at = @At("TAIL"))
    private void addFluidInfo(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced,
                              CallbackInfo ci) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(FLUID_NBT_KEY, Constants.NBT.TAG_COMPOUND)) {
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(FLUID_NBT_KEY));
            if (fluidStack != null) {
                tooltip.add(I18n.format("sussypatches.machine.creative.tank.tooltip.fluid_stored",
                        fluidStack.getLocalizedName()));
            }
        }
    }
}
