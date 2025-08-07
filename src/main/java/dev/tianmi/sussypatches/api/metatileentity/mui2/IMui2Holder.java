package dev.tianmi.sussypatches.api.metatileentity.mui2;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

public interface IMui2Holder extends IGuiHolder<PosGuiData> {

    @Override
    ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings);

    default boolean useMui2() {
        return true;
    }
}
