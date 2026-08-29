package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.MetaTileEntity;
import org.jetbrains.annotations.ApiStatus;

@MixinExtension(MetaTileEntity.class)
public interface SideCacheExtension {

    static SideCacheExtension cast(MetaTileEntity mte) {
        return (SideCacheExtension) mte;
    }

    default boolean isClient() {
        return isSus$client();
    }

    default boolean isServer() {
        return !isClient();
    }

    @ApiStatus.Internal
    boolean isSus$client();
}
