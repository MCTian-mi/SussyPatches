package dev.tianmi.sussypatches.common.helper;

import java.util.EnumMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import dev.tianmi.sussypatches.api.core.mixin.extension.QChestCDExtension;
import dev.tianmi.sussypatches.core.mixin.feature.interactivestorage.CreativeChestAccessor;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;

public class QChestInteractions {

    public static final int COOL_DOWN = 10;
    public static final int NORMAL_CLICK_THRESHOLD = COOL_DOWN - 2;
    public static final int TRANSFER_ALL_THRESHOLD = COOL_DOWN - 5;

    private static final EnumMap<EnumFacing, IndexedCuboid6> HITBOXES = new EnumMap<>(EnumFacing.class);

    static { // Don't laugh, it just works.
        HITBOXES.put(EnumFacing.UP, new IndexedCuboid6(null,
                new Cuboid6(2 / 16.0, 15 / 16.0, 2 / 16.0, 14 / 16.0, 15 / 16.0, 14 / 16.0)));
        HITBOXES.put(EnumFacing.DOWN, new IndexedCuboid6(null,
                new Cuboid6(2 / 16.0, 1 / 16.0, 2 / 16.0, 14 / 16.0, 1 / 16.0, 14 / 16.0)));
        HITBOXES.put(EnumFacing.WEST, new IndexedCuboid6(null,
                new Cuboid6(1 / 16.0, 2 / 16.0, 2 / 16.0, 1 / 16.0, 14 / 16.0, 14 / 16.0)));
        HITBOXES.put(EnumFacing.EAST, new IndexedCuboid6(null,
                new Cuboid6(15 / 16.0, 2 / 16.0, 2 / 16.0, 15 / 16.0, 14 / 16.0, 14 / 16.0)));
        HITBOXES.put(EnumFacing.SOUTH, new IndexedCuboid6(null,
                new Cuboid6(2 / 16.0, 2 / 16.0, 15 / 16.0, 14 / 16.0, 14 / 16.0, 15 / 16.0)));
        HITBOXES.put(EnumFacing.NORTH, new IndexedCuboid6(null,
                new Cuboid6(2 / 16.0, 2 / 16.0, 1 / 16.0, 14 / 16.0, 14 / 16.0, 1 / 16.0)));
    }

    @SubscribeEvent
    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        // Don't do anything if it's canceled alr.
        // Let the client side pass for now.
        if (event.isCanceled()) return;

        // IDK what could case this but well
        var hitFace = event.getFace();
        if (hitFace == null) return;

        var pos = event.getPos();
        var mte = GTUtility.getMetaTileEntity(event.getWorld(), pos);
        if (mte instanceof MetaTileEntityQuantumChest qChest &&
                // Must be the front face
                hitFace == qChest.getFrontFacing() &&
                // Front face must not be blocked
                qChest.getCoverAtSide(hitFace) == null) {

            EntityPlayer player = event.getEntityPlayer();

            // Check if the inner hitbox is hit
            RayTraceResult rayTraceResult = traceCuboidAt(pos, player, hitFace);
            if (rayTraceResult == null) return; // The cuboid is a thin layer so no need to check hitSide

            // Cancels the event anyway.
            // On the client side you just can't get what's in the qChest.
            // This is a workaround for S-C desync when breaking the qChest.
            // As a trade-off you can no longer break it when clicking within the hitbox.
            // Hope this won't cause more severe issues...
            event.setCanceled(true);

            // Special treatment for cChest
            if (qChest instanceof MetaTileEntityCreativeChest cChest) {
                cChestLeftClick(cChest, player);
            } else {
                // Cancel the event if any action is performed successfully
                qChestLeftClick(qChest, player);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        // And since all logics here are server-side only, we just skip the client side.
        // This is a workaround for S-C desync in display number updating after item insertion.
        // Hope this won't cause more severe issues...
        if (event.isCanceled() || event.getSide() != Side.SERVER) return;

        // Return false if player is sneaking, since we still want to
        // make it possible for players to place blocks/covers on this face
        var player = event.getEntityPlayer();
        if (player.isSneaking()) return;

        var hitFace = event.getFace();
        if (hitFace == null) return;

        var pos = event.getPos();
        var mte = GTUtility.getMetaTileEntity(event.getWorld(), pos);
        if (mte instanceof MetaTileEntityQuantumChest qChest &&
                hitFace == qChest.getFrontFacing() &&
                qChest.getCoverAtSide(hitFace) == null) {

            RayTraceResult rayTraceResult = traceCuboidAt(pos, player, hitFace);
            if (rayTraceResult == null) return;

            // This fails sliently for the cChest
            if (qChestRightClick(qChest, player, event.getHand())) {
                event.setCanceled(true);
            } else {
                // Denies item & block actions, whatever.
                // Might be good for compatibility.
                event.setUseBlock(Result.DENY);
                event.setUseItem(Result.DENY);
            }
        }
    }

    private static RayTraceResult traceCuboidAt(BlockPos pos, EntityPlayer player, EnumFacing facing) {
        return RayTracer.rayTrace(pos, new Vector3(RayTracer.getStartVec(player)),
                new Vector3(RayTracer.getEndVec(player)), HITBOXES.get(facing));
    }

    public static void qChestLeftClick(MetaTileEntityQuantumChest qChest, EntityPlayer player) {
        // The outputInventory only includes the output slot.
        // Here it can be used since we extract 1 stack per time at most.
        // (You can figure out why combinedInventory isn't used here, right?)
        var qChestInv = qChest.getOutputItemInventory();
        var qExtension = QChestCDExtension.cast(qChest);

        int coolDown = qExtension.sus$getCoolDown();
        qExtension.sus$refreshCoolDown(); // Refresh the cooldown
        // A workaround for getting multiple items in 1 single tick
        if (coolDown > NORMAL_CLICK_THRESHOLD) return;

        var candidate = qChestInv.extractItem(0, 1, true);
        if (candidate.isEmpty()) return;
        var stack = qChestInv.extractItem(0, player.isSneaking() ? candidate.getMaxStackSize() : 1, false);
        giveItemToPlayer(player, stack, player.inventory.currentItem,
                qChest.getPos().offset(qChest.getFrontFacing()));
    }

    public static void cChestLeftClick(MetaTileEntityCreativeChest cChest, EntityPlayer player) {
        var cChestInv = ((CreativeChestAccessor) cChest).getHandler();
        var cExtension = QChestCDExtension.cast(cChest);

        int coolDown = cExtension.sus$getCoolDown();
        cExtension.sus$refreshCoolDown();
        if (coolDown > NORMAL_CLICK_THRESHOLD) return;

        var stack = cChestInv.getStackInSlot(0).copy();
        stack.setCount(player.isSneaking() ? stack.getMaxStackSize() : 1);
        giveItemToPlayer(player, stack, player.inventory.currentItem,
                cChest.getPos().offset(cChest.getFrontFacing()));
    }

    public static boolean qChestRightClick(MetaTileEntityQuantumChest qChest, EntityPlayer player, EnumHand hand) {
        // The combinedInventory includes the output handler (slot 0) and the internal qStorage handler (slot 1).
        var qChestInv = qChest.getCombinedInventory();
        var qExtension = QChestCDExtension.cast(qChest);

        int coolDown = qExtension.sus$getCoolDown();
        qExtension.sus$refreshCoolDown();
        if (coolDown > NORMAL_CLICK_THRESHOLD) return false;

        if (coolDown > TRANSFER_ALL_THRESHOLD) { // Transfer all
            // Peek the inventory of the qChest. If nothing is found, we cannot decide
            // what to insert, so return false.
            ItemStack candidate = qChestInv.extractItem(0, 1, true);
            if (candidate.isEmpty()) return false;

            // Wraps player inventory and try transferring all itemStacks into the qChest
            var playerInv = new PlayerMainInvWrapper(player.inventory);
            GTTransferUtils.moveInventoryItems(playerInv, qChestInv);
            return true; // Return true regardless.
        } else { // Insert the held itemStack
            // Check if player is holding anything on its hand, and return false if empty.
            ItemStack sourceStack = player.getHeldItem(hand);
            if (sourceStack.isEmpty()) return false;

            // Check if held item can be inserted into the qChest.
            // If not so, return false.
            ItemStack candidate = qChestInv.insertItem(1, sourceStack, true);
            if (sourceStack.getCount() == candidate.getCount()) return false;

            ItemStack remining = qChestInv.insertItem(1, sourceStack, false);
            sourceStack.setCount(remining.getCount());
            return true;
        }
    }

    /// Basically just copied from [ItemHandlerHelper]
    @SuppressWarnings("DuplicatedCode")
    private static void giveItemToPlayer(EntityPlayer player, @NotNull ItemStack stack, int preferredSlot,
                                         BlockPos dropPos) {
        if (stack.isEmpty()) return;
        stack.setAnimationsToGo(5);

        var inventory = new PlayerMainInvWrapper(player.inventory);
        var world = player.world;

        // Try adding it into the inventory
        var remainder = stack;

        // Insert into preferred slot first
        if (preferredSlot >= 0 && preferredSlot < inventory.getSlots()) {
            remainder = inventory.insertItem(preferredSlot, stack, false);
        }

        // Then into the inventory in general
        if (!remainder.isEmpty()) {
            remainder = ItemHandlerHelper.insertItemStacked(inventory, remainder, false);
        }

        double x = dropPos.getX() + 0.5, y = dropPos.getY() + 0.5, z = dropPos.getZ() + 0.5;

        // Play sound if something got picked up
        if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
            world.playSound(null, x, y, z,
                    SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        // Drop remaining itemStack into the world
        if (!remainder.isEmpty()) {
            var entityItem = new EntityItem(world, x, y, z, remainder);
            entityItem.setPickupDelay(40);
            entityItem.motionX = 0;
            entityItem.motionY = 0;
            entityItem.motionZ = 0;
            world.spawnEntity(entityItem);
        }
    }
}
