package dev.tianmi.sussypatches.api.metatileentity;

import net.minecraft.block.SoundType;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import dev.tianmi.sussypatches.api.core.mixin.extension.SoundTypeExtension;

@ApiStatus.AvailableSince("1.4.0")
public interface ICustomSoundType extends SoundTypeExtension {

    @Override
    @NotNull
    SoundType getSoundType();
}
