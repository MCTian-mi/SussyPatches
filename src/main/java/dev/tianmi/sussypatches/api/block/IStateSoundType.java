package dev.tianmi.sussypatches.api.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.AvailableSince("1.5.0")
public interface IStateSoundType {

    @NotNull
    SoundType getSoundType(@NotNull IBlockState state);
}
