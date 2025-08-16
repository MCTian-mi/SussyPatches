package dev.tianmi.sussypatches.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.ModuleContainerRegistryEvent;
import gregtech.modules.ModuleManager;

@Mod.EventBusSubscriber
public class EventHandlers {

    @SubscribeEvent
    public static void registerModuleContainer(ModuleContainerRegistryEvent event) {
        ModuleManager.getInstance().registerContainer(new SusModules());
    }
}
