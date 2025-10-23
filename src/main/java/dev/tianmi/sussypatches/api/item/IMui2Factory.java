package dev.tianmi.sussypatches.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import dev.tianmi.sussypatches.api.core.mixin.extension.mui2.ItemUIFactoryExtension;
import dev.tianmi.sussypatches.api.mui2.factory.MetaItemGuiFactory;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;

@ApiStatus.AvailableSince("1.8.0")
public interface IMui2Factory extends ItemUIFactory, ItemUIFactoryExtension, IItemBehaviour {

    @Override
    ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager syncManager, UISettings settings);

    @Override
    default ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    default ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            MetaItemGuiFactory.openFromHand(player, hand);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }
}
