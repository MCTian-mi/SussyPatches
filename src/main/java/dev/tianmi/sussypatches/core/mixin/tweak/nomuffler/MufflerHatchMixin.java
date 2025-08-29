package dev.tianmi.sussypatches.core.mixin.tweak.nomuffler;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMufflerHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2799")
@Mixin(value = MetaTileEntityMufflerHatch.class, remap = false)
public abstract class MufflerHatchMixin extends MetaTileEntityMultiblockPart {

    @Mutable
    @Final
    @Shadow
    private int recoveryChance;

    @Mutable
    @Final
    @Shadow
    private GTItemStackHandler inventory;

    @Shadow
    private boolean frontFaceFree;

    @Shadow
    protected abstract boolean checkFrontFaceFree();

    // Dummy
    MufflerHatchMixin() {
        super(null, 0);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void removeEverything(CallbackInfo ci) {
        this.recoveryChance = -1;
        this.inventory = null;
    }

    @WrapOperation(method = "addInformation",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
                            remap = true),
                   require = -1)
    private String replaceWithRemovalLine(String translateKey, Object[] args, Operation<String> method) {
        return switch (translateKey) {
            case "gregtech.machine.muffler_hatch.tooltip1" -> method
                    .call("sussypatches.muffler.recovery_removed.tooltip", new Object[0]);
            case "gregtech.muffler.recovery_tooltip" -> method.call("sussypatches.muffler.recovery_removed.tooltip.1",
                    new Object[0]);
            default -> method.call(translateKey, args);
        };
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Override
    @Overwrite
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            if (getController() instanceof MultiblockWithDisplayBase controller && controller.isActive()) {
                VanillaParticleEffects.mufflerEffect(this, controller.getMufflerParticle());
            }
        } else if (getOffsetTimer() % 10 == 0) {
            this.frontFaceFree = checkFrontFaceFree();
        }
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Override
    @Overwrite
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Overwrite
    public void recoverItemsTable(List<ItemStack> recoveryItems) {
        SussyPatches.LOGGER.error("Class {} is trying to call \"recoverItemsTable(List<ItemStack> recoveryItems)\", " +
                "please report to the author!", this.getClass());
        throw new UnsupportedOperationException("Muffler logic has been removed!");
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Overwrite
    private boolean calculateChance() {
        SussyPatches.LOGGER.error("Class {} is trying to call \"calculateChance()\", " + "please report to the author!",
                this.getClass());
        throw new UnsupportedOperationException("Muffler logic has been removed!");
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Override
    @Overwrite
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        return super.writeToNBT(data);
    }

    /**
     * @author Tian_mi
     * @reason A hard removal of original logic
     */
    @Override
    @Overwrite
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
    }

    /**
     * @author IntegerLimit, Tian_mi
     * @reason Add warning GUI. Copied from Nomi-Libs
     */
    @Override
    @Overwrite
    protected ModularUI createUI(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 200, 100)
                .widget(new ImageWidget(92, 24, 16, 16, GuiTextures.INFO_ICON))
                .widget(new SimpleTextWidget(100, 60, "sussypatches.gui.muffler.recovery_removed", 0x1E1E1E, () -> "")
                        .setWidth(184))
                .build(getHolder(), player);
    }
}
