package dev.tianmi.sussypatches.api.core.mixin.extension;

import net.minecraft.block.SoundType;

import org.jetbrains.annotations.NotNull;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.MetaTileEntity;

@MixinExtension(MetaTileEntity.class)
public interface SoundTypeExtension {

    static SoundTypeExtension cast(MetaTileEntity mte) {
        return (SoundTypeExtension) mte;
    }

    @NotNull
    default SoundType getSoundType() {
        return SoundType.METAL;
    }
}
