package dev.tianmi.sussypatches.api.core.mixin.extension;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.metatileentity.MetaTileEntity;

/// A default impl of [IGuiHolder] for the base MTE class
/// DO NOT implement this for your mte directly!
/// Use [IMui2Holder] instead!
@MixinExtension(MetaTileEntity.class)
public interface Mui2Extension extends IGuiHolder<PosGuiData> {

    @Override
    default ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    default boolean useMui2() {
        return false;
    }
}
