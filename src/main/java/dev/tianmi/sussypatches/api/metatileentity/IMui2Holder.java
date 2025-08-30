package dev.tianmi.sussypatches.api.metatileentity;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.core.mixin.extension.Mui2Extension;

@Deprecated
@ApiStatus.AvailableSince("0.6.0")
@ApiStatus.ScheduledForRemoval(inVersion = "CEu 2.9")
public interface IMui2Holder extends Mui2Extension {

    @Override
    ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings);

    @Override
    default boolean useMui2() {
        return true;
    }
}
