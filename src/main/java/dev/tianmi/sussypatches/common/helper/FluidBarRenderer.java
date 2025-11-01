package dev.tianmi.sussypatches.common.helper;

import java.awt.*;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dev.tianmi.sussypatches.api.event.RenderItemOverlayEvent;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.ToolChargeBarRenderer;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeTank;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FluidBarRenderer {

    private static final Map<String, Set<Integer>> BLACKLIST;

    static {
        BLACKLIST = new Object2ObjectOpenHashMap<>();
        for (String item : SusConfig.FEAT.fluidBarBlacklist) {
            String[] parts = item.split("@");
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Entries in fluid bar blacklist must be a registry name, then metadata, seperated by '@'!");
            }
            int meta = Integer.parseInt(parts[1]);

            BLACKLIST.computeIfAbsent(parts[0], k -> new ObjectOpenHashSet<>()).add(meta);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderItemOverlayEvent(RenderItemOverlayEvent event) {
        event.enqueue((stack, x, y, text) -> {
            // Check blacklist
            var rl = stack.getItem().getRegistryName();
            if (rl == null) {
                return;
            }
            if (BLACKLIST.containsKey(rl.toString()) && BLACKLIST.get(rl.toString()).contains(stack.getMetadata())) {
                return;
            }

            // Getting handler usually doesn't work if itemstack has stack size > 1
            ItemStack newStack = stack.copy();
            if (newStack.getCount() > 1) {
                newStack.setCount(1);
            }

            var handler = FluidUtil.getFluidHandler(newStack);
            if (handler == null) return;

            var props = handler.getTankProperties();
            if (props.length != 1) return; // Don't handle this; only handle single-tank containers

            var fluid = props[0].getContents();
            if (fluid == null) return;

            int capacity = props[0].getCapacity();
            if (capacity <= 0) {
                // Two cases:
                // If creative tank: set capacity to fluid amount
                // Otherwise: invalid properties, return
                var mte = GTUtility.getMetaTileEntity(stack);
                if (mte instanceof MetaTileEntityCreativeTank) {
                    capacity = fluid.amount;
                } else {
                    return;
                }
            }

            // Safeguard against overflowing bars
            double level = MathHelper.clamp(fluid.amount / (double) capacity, 0.0, 1.0);
            var color = new Color(GTUtility.convertRGBtoOpaqueRGBA_MC(RenderUtil.getFluidColor(fluid)));
            ToolChargeBarRenderer.render(level, x, y, 0, true,
                    color.darker(), color.brighter(), false);
        });
    }
}
