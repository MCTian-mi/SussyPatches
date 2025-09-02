package dev.tianmi.sussypatches.common.helper;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import dev.tianmi.sussypatches.api.mui.GTGuis;
import dev.tianmi.sussypatches.api.mui.SusGuis;
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

    private static final UIFactory<GuiData> UI_FACTORY = GuiFactories.createSimple("grs_recipe_creator", Gui::new);

    @SuppressWarnings("UnstableApiUsage")
    public static void registerOverlay() { // TODO: Singleton overlay screen
        OverlayManager.register(new OverlayHandler(GuiContainerCreative.class::isInstance,
                s -> new ModularScreen(SusGuis.overlayPanel("recipe_creator_opener")
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
                                .onMousePressed(m -> {
                                    // For now slots are synced-only
                                    GuiManager.openFromClient(UI_FACTORY, new GuiData(Minecraft.getMinecraft().player));
                                    return true;
                                })))));
    }

    @SuppressWarnings("UnstableApiUsage")
    @ExtensionMethod(SusUtil.class)
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class Gui implements JeiRecipeTransferHandler, IGuiHolder<GuiData> { // TODO: Singleton

        @Setter
        @Nullable
        protected RecipeMap<?> currentMap;

        protected Dropdown<RecipeMap<?>, RecipeMapEntryWidget<?>> recipeMapSelector() {
            return new Dropdown<RecipeMap<?>, RecipeMapEntryWidget<?>>(RecipeMapEntryWidget::getWidgetValue)
                    .values(RecipeMap.getRecipeMaps(), RecipeMapEntryWidget::new)
                    .setDefault(RecipeMapEntryWidget.EMPTY)
                    .onSelected(this::setCurrentMap)
                    .interpolation(Interpolation.EXP_OUT);
        }

        @Override
        @SuppressWarnings("deprecation")
        public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
            return GTGuis.createPanel("grs_recipe_creator")
                    // .bindPlayerInventory() // TODO: Fix depth order
                    .child(recipeMapSelector());
        }

        @Nullable
        @Override
        public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
            return null; // TODO: impl transfer
        }
    }
}
