package dev.tianmi.sussypatches.common.helper;

import java.awt.*;

import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dev.tianmi.sussypatches.api.event.RenderItemOverlayEvent;
import dev.tianmi.sussypatches.api.util.SusUtil;
import dev.tianmi.sussypatches.core.mixin.feature.fluidcontainerbar.DrumAccessor;
import dev.tianmi.sussypatches.core.mixin.feature.fluidcontainerbar.QuantumTankAccessor;
import gregtech.api.util.GTUtility;
import gregtech.client.utils.RenderUtil;
import gregtech.client.utils.ToolChargeBarRenderer;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeTank;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod(SusUtil.class)
public class FluidBarRenderer {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("DataFlowIssue")
    public static void onRenderItemOverlayEvent(RenderItemOverlayEvent event) {
        event.enqueue((stack, x, y, text) -> {
            if (stack.getCount() > 1) return; // Don't draw if it's not a single item

            var mte = GTUtility.getMetaTileEntity(stack);
            int capacity;

            // We actually do not need accessor here to read capacity, but well.
            if (mte instanceof DrumAccessor drum) capacity = drum.getTankSize();
            else if (mte instanceof MetaTileEntityCreativeTank) capacity = 1000;
            else if (mte instanceof QuantumTankAccessor tank) capacity = tank.getMaxFluidCapacity();
            else return; // Don't render

            var fluid = FluidUtil.getFluidContained(stack);
            if (fluid.isEmpty()) return;

            double level = fluid.amount / (double) capacity;
            var color = new Color(GTUtility.convertRGBtoOpaqueRGBA_MC(RenderUtil.getFluidColor(fluid)));
            ToolChargeBarRenderer.render(level, x, y, 0, true,
                    color.darker(), color.brighter(), false);
        });
    }
}
