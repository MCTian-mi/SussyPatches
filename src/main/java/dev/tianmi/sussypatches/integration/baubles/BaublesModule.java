package dev.tianmi.sussypatches.integration.baubles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;

import baubles.api.cap.BaublesCapabilities;
import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;

@GregTechModule(moduleID = SusModules.BAUBLES_ID,
                containerID = Tags.MODID,
                modDependencies = Mods.Names.BAUBLES,
                name = SusModules.BAUBLES_NAME,
                description = SusModules.BAUBLES_DESC)
public class BaublesModule extends IntegrationSubmodule {

    public static IItemHandler getBaublesInvWrapper(EntityPlayer player) {
        // noinspection DataFlowIssue
        return player.getCapability(BaublesCapabilities.CAPABILITY_BAUBLES, null);
    }
}
