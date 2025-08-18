package dev.tianmi.sussypatches.common.helper;

import java.awt.*;

import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dev.tianmi.sussypatches.api.event.RenderItemOverlayEvent;
import dev.tianmi.sussypatches.core.mixin.feature.fluidcontainerbar.DrumAccessor;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.ToolChargeBarRenderer;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;

public class FluidBarRenderer {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderItemOverlayEvent(RenderItemOverlayEvent event) {
        event.enqueue((stack, x, y, text) -> {
            if (stack.getCount() > 1) return; // Don't draw if it's not a single item

            var mte = GTUtility.getMetaTileEntity(stack);
            if (!(mte instanceof MetaTileEntityDrum drum)) return;

            var fluid = FluidUtil.getFluidContained(stack);
            if (fluid == null || fluid.amount <= 0) return;

            int capacity = ((DrumAccessor) drum).getTankSize();
            double level = fluid.amount / (double) capacity;

            Color color = new Color(GTUtility.convertRGBtoOpaqueRGBA_MC(RenderUtil.getFluidColor(fluid)));
            ToolChargeBarRenderer.render(level, x, y, 0, true, color, color, false);
        });
    }
}
