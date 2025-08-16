package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;

@MixinExtension(MetaTileEntityQuantumChest.class)
public interface QChestCDExtension {

    static QChestCDExtension cast(MetaTileEntityQuantumChest qChest) {
        return (QChestCDExtension) qChest;
    }

    int sus$getCoolDown();

    void sus$refreshCoolDown();
}
