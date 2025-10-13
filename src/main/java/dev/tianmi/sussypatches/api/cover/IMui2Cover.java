package dev.tianmi.sussypatches.api.cover;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.factory.SidedPosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.core.mixin.extension.mui2.CoverExtension;

@ApiStatus.AvailableSince("1.8.0")
public interface IMui2Cover extends CoverExtension {

    @Override
    ModularPanel buildUI(SidedPosGuiData guiData, PanelSyncManager syncManager, UISettings settings);

    @Override
    boolean useMui2();
}
