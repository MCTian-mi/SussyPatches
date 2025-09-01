package dev.tianmi.sussypatches.common.helper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.gui.inventory.GuiContainerCreative;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.mui.GTGuis;
import dev.tianmi.sussypatches.api.mui.widget.Dropdown;
import dev.tianmi.sussypatches.api.mui.widget.RecipeMapEntryWidget;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.recipes.RecipeMap;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

@ExtensionMethod(SusUtil.class)
public class GrSRecipeCreator {

    @SuppressWarnings("UnstableApiUsage")
    public static void registerOverlay() {
        OverlayManager.register(new OverlayHandler(GuiContainerCreative.class::isInstance,
                $ -> new ModularScreen(ModularPanel.defaultPanel("recipe_creator_opener")
                        .full()
                        .background(IDrawable.EMPTY)
                        .child(new ButtonWidget<>()
                                .align(Alignment.BottomCenter)
                                .bottom(4)
                                .size(84, 20)
                                .overlay(IKey.lang("sussypatches.gui.recipe_creator.open")
                                        .color(Color.WHITE.main)
                                        .shadow(true)
                                        .asIcon()
                                        .center())
                                .onMousePressed(mouseButton -> {
                                    // TODO: Check JEI
                                    // var jeiSetting = new JeiSettingsImpl();
                                    // jeiSetting.enableJei();
                                    // ClientGUI.open(new Screen(), jeiSetting);
                                    ClientGUI.open(new Screen());
                                    return true;
                                })))));
    }

    @SuppressWarnings("UnstableApiUsage")
    @ExtensionMethod(SusUtil.class)
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class Screen extends CustomModularScreen implements JeiRecipeTransferHandler { // TODO: singleton

        @Setter
        @Nullable
        protected RecipeMap<?> currentMap;

        protected Screen() {
            super(Tags.MODID);
        }

        protected Dropdown<?, ?> recipeMapSelector() {
            return new Dropdown<RecipeMap<?>, RecipeMapEntryWidget<?>>(RecipeMapEntryWidget::getWidgetValue)
                    .children(RecipeMap.getRecipeMaps(), SusUtil::asWidget)
                    .setDefault(RecipeMapEntryWidget.EMPTY)
                    .onSelected(this::setCurrentMap)
                    .interpolation(Interpolation.EXP_OUT);
        }

        @Override
        @SuppressWarnings("deprecation")
        public ModularPanel buildUI(ModularGuiContext context) {
            return GTGuis.createPanel("grs_recipe_creator")
                    .child(recipeMapSelector());
        }

        @Override
        public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
            return null; // TODO: impl transfer
        }
    }
}
