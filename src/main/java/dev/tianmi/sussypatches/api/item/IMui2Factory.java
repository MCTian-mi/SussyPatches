package dev.tianmi.sussypatches.api.item;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.core.mixin.extension.mui2.ItemUIFactoryExtension;

@ApiStatus.AvailableSince("1.8.0")
public interface IMui2Factory extends ItemUIFactoryExtension {

    @Override
    ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager syncManager, UISettings settings);
}
