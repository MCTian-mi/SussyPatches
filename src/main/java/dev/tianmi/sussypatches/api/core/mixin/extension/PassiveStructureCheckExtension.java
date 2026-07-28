package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import org.jspecify.annotations.NullMarked;

@NullMarked
@MixinExtension(MultiblockControllerBase.class)
public interface PassiveStructureCheckExtension {

    static PassiveStructureCheckExtension cast(MultiblockControllerBase controllerBase) {
        return (PassiveStructureCheckExtension) controllerBase;
    }

    void sus$markStructureDirty();
}
