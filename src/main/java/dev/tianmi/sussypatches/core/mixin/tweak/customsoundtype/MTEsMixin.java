package dev.tianmi.sussypatches.core.mixin.tweak.customsoundtype;

import net.minecraft.block.SoundType;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.SoundTypeExtension;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.common.metatileentities.MetaTileEntityClipboard;
import gregtech.common.metatileentities.multi.*;
import gregtech.common.metatileentities.storage.MetaTileEntityCrate;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2853")
@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MTEsMixin implements SoundTypeExtension {

    @Mixin(value = {
            MetaTileEntityCokeOven.class,
            MetaTileEntityCokeOvenHatch.class,
            MetaTileEntityPrimitiveBlastFurnace.class,
    }, remap = false)
    private abstract static class StoneSoundMTEsMixin extends MTEsMixin {

        @Unique
        @NotNull
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public SoundType getSoundType() {
            return SoundType.STONE;
        }
    }

    @Mixin(value = {
            MetaTileEntityPrimitiveWaterPump.class,
            MetaTileEntityPumpHatch.class,
            MetaTileEntityWorkbench.class,
            MetaTileEntityClipboard.class,
    }, remap = false)
    private abstract static class WoodSoundMTEsMixin extends MTEsMixin {

        @Unique
        @NotNull
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public SoundType getSoundType() {
            return SoundType.WOOD;
        }
    }

    @Mixin(value = MetaTileEntityMultiblockTank.class, remap = false)
    private abstract static class MultiblockTankMixin extends MTEsMixin {

        @Shadow
        @Final
        private boolean isMetal;

        @Unique
        @NotNull
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public SoundType getSoundType() {
            return this.isMetal ? SoundType.METAL : SoundType.WOOD;
        }
    }

    @Mixin(value = MetaTileEntityTankValve.class, remap = false)
    private abstract static class TankValveMixin extends MTEsMixin {

        @Shadow
        @Final
        private boolean isMetal;

        @Unique
        @NotNull
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public SoundType getSoundType() {
            return this.isMetal ? SoundType.METAL : SoundType.WOOD;
        }
    }

    @Mixin(value = MetaTileEntityCrate.class, remap = false)
    private abstract static class CrateMixin extends MTEsMixin {

        @Shadow
        @Final
        private Material material;

        @Unique
        @NotNull
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public SoundType getSoundType() {
            return ModHandler.isMaterialWood(material) ? SoundType.WOOD : SoundType.METAL;
        }
    }

    @Mixin(value = MetaTileEntityDrum.class, remap = false)
    private abstract static class DrumMixin extends MTEsMixin {

        @Shadow
        @Final
        private boolean isWood;

        @Unique
        @NotNull
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public SoundType getSoundType() {
            return this.isWood ? SoundType.WOOD : SoundType.METAL;
        }
    }
}
