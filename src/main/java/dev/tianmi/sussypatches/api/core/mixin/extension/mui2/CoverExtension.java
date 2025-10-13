package dev.tianmi.sussypatches.api.core.mixin.extension.mui2;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.cover.CoverWithUI;

/// Same as [MTEExtension] but for [CoverWithUI]
/// Use [IMui2Cover]!
@ApiStatus.Internal
@MixinExtension(CoverWithUI.class)
public interface CoverExtension extends IGuiHolder<SidedPosGuiData> {

    static CoverExtension cast(CoverWithUI cover) {
        return (CoverExtension) cover;
    }

    @Override
    default ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    default boolean useMui2() {
        return false;
    }
}
