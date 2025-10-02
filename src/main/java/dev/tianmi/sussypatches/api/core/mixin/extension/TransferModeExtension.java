package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import dev.tianmi.sussypatches.core.mixin.feature.coverretainexact.TransferModeAccessor;
import gregtech.common.covers.TransferMode;

// Well this technically is an 'extension', and is mixed-in indeed, so...
@MixinExtension(TransferMode.class)
public interface TransferModeExtension {

    TransferMode RETAIN_EXACT = TransferModeAccessor.create("RETAIN_EXACT", TransferMode.values().length,
            "sussypatches.cover.transfer_mode.retain_exact", Integer.MAX_VALUE);
}
