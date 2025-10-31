package dev.tianmi.sussypatches.common.helper;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dev.tianmi.sussypatches.api.event.RenderItemOverlayEvent;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.ToolChargeBarRenderer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FluidBarRenderer {

    private static final Set<String> BLACKLIST;

    static {
        BLACKLIST = new ObjectOpenHashSet<>();
        Collections.addAll(BLACKLIST, SusConfig.FEAT.fluidBarBlacklist);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderItemOverlayEvent(RenderItemOverlayEvent event) {
        event.enqueue((stack, x, y, text) -> {
            // Check blacklist
            var rl = stack.getItem().getRegistryName();
            if (rl == null || BLACKLIST.contains(rl.toString()))
                return;

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

            double level = fluid.amount / (double) capacity;
            var color = new Color(GTUtility.convertRGBtoOpaqueRGBA_MC(RenderUtil.getFluidColor(fluid)));
            ToolChargeBarRenderer.render(level, x, y, 0, true,
                    color.darker(), color.brighter(), false);
        });
    }
}
