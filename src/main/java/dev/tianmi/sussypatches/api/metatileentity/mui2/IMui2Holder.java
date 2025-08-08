package dev.tianmi.sussypatches.api.metatileentity.mui2;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.core.mixin.extension.Mui2Extension;

@ApiStatus.AvailableSince("0.6.0")
public interface IMui2Holder extends Mui2Extension {

    @Override
    ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings);

    @Override
    default boolean useMui2() {
        return true;
    }
}
