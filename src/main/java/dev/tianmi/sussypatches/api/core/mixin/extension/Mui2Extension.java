package dev.tianmi.sussypatches.api.core.mixin.extension;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import dev.tianmi.sussypatches.api.metatileentity.mui2.IMui2Holder;
import gregtech.api.metatileentity.MetaTileEntity;

/// A default impl of [IMui2Holder] for the base MTE class
/// DO NOT implement this for your mte directly!
/// Use [IMui2Holder] instead!
@MixinExtension(MetaTileEntity.class)
public interface Mui2Extension extends IMui2Holder {

    @Override
    default ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        return null;
    }

    @Override
    default boolean useMui2() {
        return false;
    }
}
