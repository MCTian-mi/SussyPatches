package dev.tianmi.sussypatches;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.tianmi.sussypatches.api.util.SusAccessor;
import dev.tianmi.sussypatches.common.CommonProxy;
import gregtech.GTInternalTags;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     dependencies = GTInternalTags.DEP_VERSION_STRING + "after:ctm;" + "after:configanytime;",
     acceptedMinecraftVersions = ForgeVersion.mcVersion)
public class SussyPatches {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @SidedProxy(modId = Tags.MODID,
                clientSide = "dev.tianmi.sussypatches.client.ClientProxy",
                serverSide = "dev.tianmi.sussypatches.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Tags.MODID)
    public static SussyPatches instance;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        proxy.onPreInit();
        SusAccessor.set(Minecraft.class, Boolean.class, Minecraft.getMinecraft(), "isGamePaused", true);
        LOGGER.info(SusAccessor.get(Minecraft.class, Boolean.class, Minecraft.getMinecraft(), "isGamePaused"));
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        proxy.onInit();
    }

    @EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.onPostInit();
    }
}
