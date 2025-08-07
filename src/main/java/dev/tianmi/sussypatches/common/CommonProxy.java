package dev.tianmi.sussypatches.common;

import net.minecraftforge.fml.common.Mod;

import com.cleanroommc.modularui.factory.GuiManager;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.metatileentity.mui2.MTEGuiFactory;
import dev.tianmi.sussypatches.api.util.SusMods;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public class CommonProxy {

    public void onPreInit() {
        if (SusMods.ModularUI.isLoaded() && SusConfig.API.useMui2) {
            GuiManager.registerFactory(MTEGuiFactory.INSTANCE);
        }
    }

    public void onInit() {}

    public void onPostInit() {}
}
