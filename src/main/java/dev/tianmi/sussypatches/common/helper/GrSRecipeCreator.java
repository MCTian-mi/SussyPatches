package dev.tianmi.sussypatches.common.helper;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.*;
import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.EmptyWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import dev.tianmi.sussypatches.api.mui.GTGuis;
import dev.tianmi.sussypatches.api.mui.SusGuis;
import dev.tianmi.sussypatches.api.mui.widget.Dropdown;
import dev.tianmi.sussypatches.api.mui.widget.RecipeMapEntryWidget;
import dev.tianmi.sussypatches.api.mui.widget.RecipeProgressWidget;
import dev.tianmi.sussypatches.api.util.SusUtil;
import dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator.RecipeLayoutAccessor;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.recipes.RecipeMap;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.experimental.ExtensionMethod;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.transfer.RecipeTransferErrorTooltip;

@ExtensionMethod(SusUtil.class)
public class GrSRecipeCreator {

    private static final UIFactory<GuiData> UI_FACTORY = GuiFactories.createSimple("grs_recipe_creator", Gui::new);

    @SuppressWarnings("UnstableApiUsage")
    public static void registerOverlay() { // TODO)) Singleton overlay screen
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
    public static class Gui implements IGuiHolder<GuiData> { // TODO)) Singleton

        @Nullable
        protected RecipeMap<?> currentMap;

        protected long eut = 0;
        protected int duration = 0;

        protected int maxInputs = 0;
        protected int maxOutputs = 0;
        protected IItemHandlerModifiable importItems = new ItemStackHandler(0);
        protected IItemHandlerModifiable exportItems = new ItemStackHandler(0);

        protected int maxFluidInputs = 0;
        protected int maxFluidOutputs = 0;
        protected FluidTankList importFluids = new FluidTankList(false);
        protected FluidTankList exportFluids = new FluidTankList(false);

        protected void setCurrentMap(RecipeMap<?> recipeMap) {
            this.currentMap = recipeMap;
            initInventories();
        }

        public void initInventories() {
            if (currentMap == null) return;
            this.eut = 0;
            this.duration = 0;

            this.maxInputs = currentMap.getMaxInputs();
            this.maxOutputs = currentMap.getMaxOutputs();
            this.importItems = new ItemStackHandler(maxInputs);
            this.exportItems = new ItemStackHandler(maxOutputs);

            this.maxFluidInputs = currentMap.getMaxFluidInputs();
            this.maxFluidOutputs = currentMap.getMaxFluidOutputs();
            this.importFluids = SusUtil.createTankList(maxFluidInputs);
            this.exportFluids = SusUtil.createTankList(maxFluidOutputs);
        }

        protected Dropdown<RecipeMap<?>, RecipeMapEntryWidget<?>> recipeMapSelector() {
            return new Dropdown<RecipeMap<?>, RecipeMapEntryWidget<?>>(RecipeMapEntryWidget::getWidgetValue)
                    .values(RecipeMap.getRecipeMaps(), RecipeMapEntryWidget::new)
                    .setDefault(RecipeMapEntryWidget.EMPTY)
                    .onSelected(this::setCurrentMap)
                    .interpolation(Interpolation.EXP_OUT);
        }

        protected IWidget recipeMapGui() {
            if (currentMap == null) return new EmptyWidget();

            var inputItemsMatrix = Grid.mapToMatrix(3, maxInputs,
                    i -> new ItemSlot().slot(importItems, i));
            var outputItemsMatrix = Grid.mapToMatrix(3, maxOutputs,
                    i -> new ItemSlot().slot(exportItems, i));
            var inputFluidsMatrix = Grid.mapToMatrix(3, maxFluidInputs,
                    i -> new FluidSlot().syncHandler(importFluids.getTankAt(i)));
            var outputFluidsMatrix = Grid.mapToMatrix(3, maxFluidOutputs,
                    i -> new FluidSlot().syncHandler(exportFluids.getTankAt(i)));

            return Flow.row()
                    .coverChildren()
                    .collapseDisabledChild()
                    .background(GuiTextures.MC_BACKGROUND)
                    .padding(8)
                    .childPadding(4)
                    .child(Flow.column()
                            .coverChildren()
                            .collapseDisabledChild()
                            .child(new Grid()
                                    .matrix(inputItemsMatrix)
                                    .coverChildren())
                            .child(new Grid()
                                    .matrix(inputFluidsMatrix)
                                    .coverChildren()))
                    .child(new RecipeProgressWidget()
                            .recipeMap(currentMap)
                            .debugName("recipe.progress")
                            .progress(0)
                            .texture(currentMap.getProgressBar(), 20)
                            .size(20))
                    .child(Flow.column()
                            .coverChildren()
                            .collapseDisabledChild()
                            .child(new Grid()
                                    .matrix(outputItemsMatrix)
                                    .coverChildren())
                            .child(new Grid()
                                    .matrix(outputFluidsMatrix)
                                    .coverChildren()));
        }

        @Override
        @SuppressWarnings("deprecation")
        public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
            return GTGuis.createPanel("grs_recipe_creator")
                    .child(Flow.column()
                            .coverChildren()
                            .child(recipeMapSelector())
                            .child(recipeMapGui()))
                    .bindPlayerInventory(); // TODO)) Fix depth order
        }

        @SideOnly(Side.CLIENT)
        public ModularScreen createScreen(GuiData data, ModularPanel mainPanel) {
            return new Screen(mainPanel);
        }

        public class Screen extends ModularScreen implements JeiRecipeTransferHandler {

            public Screen(ModularPanel mainPanel) {
                super(mainPanel);
            }

            @Nullable
            @Override
            public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer,
                                                       boolean simulate) {
                if (!(recipeLayout.getRecipeCategory() instanceof RecipeMapCategory)) {
                    return new RecipeTransferErrorTooltip(
                            IKey.lang("sussypatches.gui.recipe_creator.transfer_error").get());
                }

                if (simulate) return null;

                initInventories();

                int inputSlot = 0, outputSlot = 0;
                var items = new Int2ObjectArrayMap<>(recipeLayout.getItemStacks().getGuiIngredients());
                for (var ingredient : items.values()) {
                    var candidate = ingredient.getDisplayedIngredient();
                    if (candidate == null || candidate.isEmpty()) continue;

                    if (ingredient.isInput()) {
                        if (inputSlot >= maxInputs) continue;
                        importItems.setStackInSlot(inputSlot++, candidate);
                    } else {
                        if (outputSlot >= maxInputs) continue;
                        exportItems.setStackInSlot(outputSlot++, candidate);
                    }
                }

                inputSlot = outputSlot = 0;
                var fluids = new Int2ObjectArrayMap<>(recipeLayout.getFluidStacks().getGuiIngredients());
                for (var ingredient : fluids.values()) {
                    var candidate = ingredient.getDisplayedIngredient();
                    if (candidate.isEmpty()) continue;

                    if (ingredient.isInput()) {
                        if (inputSlot >= maxInputs) continue;
                        var tank = importFluids.getTankAt(inputSlot);
                        tank.drain(Integer.MAX_VALUE, true);
                        tank.fill(candidate, true);
                        inputSlot++;
                    } else {
                        if (outputSlot >= maxInputs) continue;
                        var tank = exportFluids.getTankAt(outputSlot);
                        tank.drain(Integer.MAX_VALUE, true);
                        tank.fill(candidate, true);
                        outputSlot++;
                    }
                }

                if (recipeLayout instanceof RecipeLayoutAccessor layout &&
                        layout.getRecipeWrapper() instanceof GTRecipeWrapper recipeWrapper) {
                    var recipe = recipeWrapper.getRecipe();

                    Gui.this.eut = recipe.getEUt();
                    Gui.this.duration = recipe.getDuration();
                }

                return null;
            }
        }
    }
}
