package dev.tianmi.sussypatches.core.mixin.tweak.customsoundtype;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.block.IStateSoundType;
import gregtech.common.blocks.BlockSteamCasing.SteamCasingType;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2853")
@Mixin(value = SteamCasingType.class, remap = false)
public abstract class SteamCasingTypeMixin implements IStateSoundType {

    @Unique
    private SoundType sus$soundType;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initSoundType(String enumName, int ordinal, String name, int harvestLevel, CallbackInfo ci) {
        this.sus$soundType = name.equals("pump_deck") || name.equals("wood_wall") ?
                SoundType.WOOD : SoundType.METAL;
    }

    @Unique
    @NotNull
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public SoundType getSoundType(@NotNull IBlockState state) {
        return sus$soundType;
    }
}
