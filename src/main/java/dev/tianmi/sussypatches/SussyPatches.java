package dev.tianmi.sussypatches;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.common.CommonProxy;
import gregtech.GTInternalTags;
import gregtech.api.util.Mods;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     dependencies = SussyPatches.DEP_STRING,
     acceptedMinecraftVersions = ForgeVersion.mcVersion)
public class SussyPatches {

    private static final String AFTER = ";after:";

    static final String DEP_STRING = GTInternalTags.DEP_VERSION_STRING +
            AFTER + Mods.Names.CONNECTED_TEXTURES_MOD +
            AFTER + SusMods.Names.CONFIGANYTIME;

    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    @SidedProxy(modId = Tags.MODID,
                clientSide = "dev.tianmi.sussypatches.client.ClientProxy",
                serverSide = "dev.tianmi.sussypatches.common.CommonProxy")
    public static CommonProxy proxy;

    @Instance(Tags.MODID)
    public static SussyPatches instance;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        proxy.onPreInit();
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
