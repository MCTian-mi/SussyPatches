package dev.tianmi.sussypatches.common.helper;

import java.awt.*;
import java.util.function.Predicate;

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

public class FluidBarRenderer {

    private static final Predicate<ItemStack> filter = SusConfig.FEAT.fluidBarRenderer.filter();

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderItemOverlayEvent(RenderItemOverlayEvent event) {
        event.enqueue((itemStack, x, y, _) -> {

            // Check list filter
            if (!filter.test(itemStack)) return;

            // Getting handler usually doesn't work if itemStack has stack size > 1
            var singleStack = itemStack.copy();
            if (singleStack.getCount() > 1) singleStack.setCount(1);

            var handler = FluidUtil.getFluidHandler(singleStack);
            if (handler == null) return;

            var tankProps = handler.getTankProperties();
            if (tankProps.length != 1) return; // Don't handle this; only handle single-tank containers

            var tankProp = tankProps[0];
            var fluidStack = tankProp.getContents();
            if (fluidStack == null) return;

            int amount = fluidStack.amount;
            int capacity = tankProp.getCapacity();
            if (capacity <= 0) {
                // Two cases:
                // If creative tank: set capacity to fluid amount
                // Otherwise: invalid properties, return
                if (GTUtility.getMetaTileEntity(itemStack) instanceof MetaTileEntityCreativeTank) {
                    capacity = amount;
                } else {
                    return;
                }
            }

            // Safeguard against overflowing bars
            double level = MathHelper.clamp(amount / (double) capacity, 0.0, 1.0);
            var color = new Color(GTUtility.convertRGBtoOpaqueRGBA_MC(RenderUtil.getFluidColor(fluidStack)));
            ToolChargeBarRenderer.render(level, x, y, 0, true,
                    color.darker(), color.brighter(), false);
        });
    }
}
