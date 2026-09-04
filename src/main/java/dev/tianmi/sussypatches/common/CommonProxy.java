package dev.tianmi.sussypatches.common;

import dev.tianmi.sussypatches.Tags;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class CommonProxy {

    public void onPreInit() {}

    public void onInit() {}

    public void onPostInit() {}
    
    @SubscribeEvent
    public static void syncConfig(ConfigChangedEvent.OnConfigChangedEvent event)
    {
    	if(event.getModID().equals(Tags.MOD_ID))
    	{
    		ConfigManager.sync(Tags.MOD_ID, Type.INSTANCE);
    	}
    }
}
