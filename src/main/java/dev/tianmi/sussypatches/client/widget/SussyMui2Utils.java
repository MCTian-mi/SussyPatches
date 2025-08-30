package dev.tianmi.sussypatches.client.widget;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import gregtech.api.fluids.GTFluid;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "Next CEu update")
public class SussyMui2Utils {

    @NotNull
    public static IKey fluid(@Nullable Fluid fluid) {
        return fluid(fluid, null);
    }

    @NotNull
    public static IKey fluid(@Nullable Fluid fluid, @Nullable FluidStack stack) {
        if (fluid == null) return IKey.EMPTY;
        if (fluid instanceof GTFluid.GTMaterialFluid gtFluid) {
            return getLocalizedKey(gtFluid);
        }
        if (stack == null) return IKey.lang(fluid.getUnlocalizedName());
        else return IKey.lang(fluid.getUnlocalizedName(stack));
    }

    @NotNull
    public static IKey fluid(@Nullable FluidStack fluid) {
        if (fluid == null) return IKey.EMPTY;
        return fluid(fluid.getFluid(), fluid);
    }

    public static @NotNull IKey getLocalizedKey(GTFluid.GTMaterialFluid gtFluid) {
        IKey localizedName;
        String customMaterialTranslation = "fluid." + gtFluid.getMaterial().getUnlocalizedName();

        if (net.minecraft.util.text.translation.I18n.canTranslate(customMaterialTranslation)) {
            localizedName = IKey.lang(customMaterialTranslation);
        } else {
            localizedName = IKey.lang(gtFluid.getMaterial().getUnlocalizedName());
        }
        return localizedName;
    }

    public static ModularPanel createPopupPanel(String name, int width, int height) {
        return createPopupPanel(name, width, height, false, false);
    }

    public static ModularPanel createPopupPanel(
                                                String name, int width, int height, boolean disableBelow,
                                                boolean closeOnOutsideClick) {
        return new PopupPanel(name, width, height, disableBelow, closeOnOutsideClick);
    }

    public static class PopupPanel extends ModularPanel {

        private final boolean disableBelow;
        private final boolean closeOnOutsideClick;

        public PopupPanel(@NotNull String name, int width, int height, boolean disableBelow,
                          boolean closeOnOutsideClick) {
            super(name);
            size(width, height).align(Alignment.Center);
            background(GuiTextures.MC_BACKGROUND);
            child(ButtonWidget.panelCloseButton().top(5).right(5)
                    .onMousePressed(mouseButton -> {
                        if (mouseButton == 0 || mouseButton == 1) {
                            this.closeIfOpen();
                            return true;
                        }
                        return false;
                    }));
            this.disableBelow = disableBelow;
            this.closeOnOutsideClick = closeOnOutsideClick;
        }

        @Override
        public boolean disablePanelsBelow() {
            return disableBelow;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return closeOnOutsideClick;
        }
    }
}
