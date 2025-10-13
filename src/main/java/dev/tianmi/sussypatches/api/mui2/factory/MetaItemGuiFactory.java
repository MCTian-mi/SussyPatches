package dev.tianmi.sussypatches.api.mui2.factory;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.factory.inventory.InventoryType;
import com.cleanroommc.modularui.factory.inventory.InventoryTypes;
import com.cleanroommc.modularui.utils.Platform;

import dev.tianmi.sussypatches.api.core.mixin.extension.mui2.ItemUIFactoryExtension;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.Mods;

/// Basically just [PlayerInventoryGuiFactory]
public class MetaItemGuiFactory extends AbstractUIFactory<PlayerInventoryGuiData> {

    public static final MetaItemGuiFactory INSTANCE = new MetaItemGuiFactory();

    private MetaItemGuiFactory() {
        super("gregtech:meta_item");
    }

    public static void open(EntityPlayer player, InventoryType type, int index) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(type);
        GuiManager.open(INSTANCE, new PlayerInventoryGuiData(player, type, index), verifyServerSide(player));
    }

    public static void openFromPlayerInventory(EntityPlayer player, int index) {
        open(player, InventoryTypes.PLAYER, index);
    }

    public static void openFromHand(EntityPlayer player, EnumHand hand) {
        openFromPlayerInventory(player, hand == EnumHand.OFF_HAND ? 40 : player.inventory.currentItem);
    }

    public static void openFromBaubles(EntityPlayer player, int index) {
        if (!Mods.Baubles.isModLoaded()) {
            throw new IllegalArgumentException("Can't open UI for baubles item when bauble is not loaded!");
        }
        open(player, InventoryTypes.BAUBLES, index);
    }

    @SideOnly(Side.CLIENT)
    public static void openClient(InventoryType type, int index) {
        Objects.requireNonNull(type);
        GuiManager.openFromClient(INSTANCE, new PlayerInventoryGuiData(Platform.getClientPlayer(), type, index));
    }

    @SideOnly(Side.CLIENT)
    public static void openFromPlayerInventoryClient(int index) {
        openClient(InventoryTypes.PLAYER, index);
    }

    @SideOnly(Side.CLIENT)
    public static void openFromHandClient(EnumHand hand) {
        openFromPlayerInventoryClient(
                hand == EnumHand.OFF_HAND ? 40 : Platform.getClientPlayer().inventory.currentItem);
    }

    @SideOnly(Side.CLIENT)
    public static void openFromBaublesClient(EntityPlayer player, int index) {
        if (!Mods.Baubles.isModLoaded()) {
            throw new IllegalArgumentException("Can't open UI for baubles item when bauble is not loaded!");
        }
        openClient(InventoryTypes.BAUBLES, index);
    }

    @NotNull
    @Override
    public IGuiHolder<PlayerInventoryGuiData> getGuiHolder(PlayerInventoryGuiData data) {
        ItemStack stack = data.getUsedItemStack();
        if (!(stack.getItem() instanceof MetaItem<?>metaItem)) {
            throw new IllegalArgumentException("Found item is not a valid MetaItem!");
        }
        MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(stack);
        if (valueItem == null || valueItem.getUIManager() == null) {
            throw new IllegalArgumentException("Found MetaItem is not a gui holder!");
        }

        return ItemUIFactoryExtension.cast(valueItem.getUIManager());
    }

    @Override
    public void writeGuiData(PlayerInventoryGuiData guiData, PacketBuffer buffer) {
        guiData.getInventoryType().write(buffer);
        buffer.writeVarInt(guiData.getSlotIndex());
    }

    @NotNull
    @Override
    public PlayerInventoryGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        return new PlayerInventoryGuiData(player, InventoryType.read(buffer), buffer.readVarInt());
    }
}
