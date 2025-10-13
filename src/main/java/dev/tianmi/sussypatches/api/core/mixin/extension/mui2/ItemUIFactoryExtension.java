package dev.tianmi.sussypatches.api.core.mixin.extension.mui2;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.items.gui.ItemUIFactory;

/// Same as [MTEExtension] but for [ItemUIFactory]
/// Use [IMui2Factory]!
@ApiStatus.Internal
@MixinExtension(ItemUIFactory.class)
public interface ItemUIFactoryExtension extends IGuiHolder<PlayerInventoryGuiData> {

    static ItemUIFactoryExtension cast(ItemUIFactory uiFactory) {
        return (ItemUIFactoryExtension) uiFactory;
    }

    @Override
    default ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }
}
