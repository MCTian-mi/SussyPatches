package dev.tianmi.sussypatches.common.helper;

import static gregtech.common.items.MetaItems.*;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import dev.tianmi.sussypatches.api.core.mixin.extension.SpecialModelExtension;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;

public class VisibleFluidCell {

    private static final int PIXEL_VAIL = 11;
    private static final int PIXEL_CELL = 10;
    private static final int PIXEL_CELL_LARGE = 14;

    public static void changeModelAmount(MetaValueItem fluidCell, int pixel) {
        SpecialModelExtension.cast(fluidCell.setModelAmount((pixel - 1) * 2 /* Liquid + gas */ + 1 /* Full */))
                .setItemModelDispatcher((itemStack, _) -> {
                    var singleStack = itemStack.copy();
                    if (singleStack.getCount() > 1) singleStack.setCount(1);

                    var handler = FluidUtil.getFluidHandler(singleStack);
                    if (handler == null) return pixel;

                    var tankProps = handler.getTankProperties();
                    if (tankProps.length != 1) return pixel;

                    var tankProp = tankProps[0];
                    var fluidStack = tankProp.getContents();
                    if (fluidStack == null) return pixel;

                    boolean isGas = fluidStack.getFluid().isGaseous();
                    int amount = fluidStack.amount;
                    int capacity = tankProp.getCapacity();

                    return isGas ? 2 * pixel - 2 - ((pixel - 1) * amount) / capacity :
                            ((pixel - 1) * amount) / capacity + 1;
                });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemRegistration(RegistryEvent.Register<Item> event) {
        changeModelAmount(FLUID_CELL_GLASS_VIAL, PIXEL_VAIL);
        changeModelAmount(FLUID_CELL_UNIVERSAL, PIXEL_CELL);
        changeModelAmount(FLUID_CELL_LARGE_STEEL, PIXEL_CELL_LARGE);
        changeModelAmount(FLUID_CELL_LARGE_ALUMINIUM, PIXEL_CELL_LARGE);
        changeModelAmount(FLUID_CELL_LARGE_STAINLESS_STEEL, PIXEL_CELL_LARGE);
        changeModelAmount(FLUID_CELL_LARGE_TITANIUM, PIXEL_CELL_LARGE);
        changeModelAmount(FLUID_CELL_LARGE_TUNGSTEN_STEEL, PIXEL_CELL_LARGE);
    }
}
