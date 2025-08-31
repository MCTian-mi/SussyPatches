package dev.tianmi.sussypatches.common.helper;

import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.gui.inventory.GuiContainerCreative;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.mui.widget.scroll.VanillaScrollData;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.recipes.RecipeMap;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

@ExtensionMethod(SusUtil.class)
public enum GrSRecipeCreator {

    INSTANCE;

    @SuppressWarnings("UnstableApiUsage")
    public void register() {
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
    public static class Screen extends CustomModularScreen implements JeiRecipeTransferHandler {

        @Setter
        @Nullable
        protected RecipeMap<?> currentMap;

        private Screen() {
            super(Tags.MODID);
        }

        // TODO: Cleanup logic
        @Override
        public ModularPanel buildUI(ModularGuiContext context) {
            var listWidget = new ListWidget<>();
            var buttonWidget = new ButtonWidget<>()
                    .size(120, 20)
                    .onMousePressed(key -> {
                        listWidget.setEnabled(!listWidget.isEnabled());
                        return true;
                    });

            listWidget.margin(6)
                    .size(120, 160)
                    .scrollDirection(new VanillaScrollData(10))
                    .children(RecipeMap.getRecipeMaps()
                            .stream()
                            .map(map -> new ButtonWidget<>()
                                    .widthRel(1)
                                    .height(20)
                                    .child(map.asWidget())
                                    .onMousePressed(key -> {
                                        setCurrentMap(map);
                                        buttonWidget.child(map.asWidget())
                                                .scheduleResize();
                                        listWidget.disabled();
                                        // TODO: Close parent widget
                                        return true;
                                    }))
                            .collect(Collectors.toSet()));

            return ModularPanel.defaultPanel("tutorial_panel")
                    .child(Flow.column()
                            .background(IDrawable.EMPTY)
                            .coverChildren()
                            .collapseDisabledChild()
                            .child(buttonWidget)
                            .child(Flow.column()
                                    .setEnabledIf($ -> listWidget.isEnabled())
                                    .background(GuiTextures.MC_BACKGROUND)
                                    .coverChildren()
                                    .collapseDisabledChild()
                                    .child(listWidget)));
        }

        @Override
        public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
            return null;
        }
    }
}
