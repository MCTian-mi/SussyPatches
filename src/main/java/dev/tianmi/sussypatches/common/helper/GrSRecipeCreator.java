package dev.tianmi.sussypatches.common.helper;

import java.util.Comparator;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.I18n;
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
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.Interpolation;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import dev.tianmi.sussypatches.api.mui.GTGuis;
import dev.tianmi.sussypatches.api.mui.SusGuis;
import dev.tianmi.sussypatches.api.mui.widget.Dropdown;
import dev.tianmi.sussypatches.api.mui.widget.PhantomFluidSlot;
import dev.tianmi.sussypatches.api.mui.widget.RecipeMapEntryWidget;
import dev.tianmi.sussypatches.api.mui.widget.RecipeProgressWidget;
import dev.tianmi.sussypatches.api.util.BoolSupplier;
import dev.tianmi.sussypatches.api.util.SusUtil;
import dev.tianmi.sussypatches.core.mixin.compat.grsrecipecreator.RecipeLayoutAccessor;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.RecipeMap;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.config.ServerInfo;
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
                                    GuiManager.openFromClient(UI_FACTORY, new GuiData(Minecraft.getMinecraft().player));
                                    return true;
                                })))));
    }

    @SuppressWarnings({ "UnstableApiUsage", "deprecation" })
    @ExtensionMethod(SusUtil.class)
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class Gui implements IGuiHolder<GuiData> { // TODO)) Singleton

        protected static final int SLOTS = 16; // Enough for most recipe maps
        protected static final int ROW_LENGTH = 3;

        @Getter
        @Nullable
        protected RecipeMap<?> currentMap;

        protected final ValueSyncHandler<RecipeMap<?>> recipeMapValue = new GenericSyncValue<>(this::getCurrentMap,
                this::setCurrentMap,
                buf -> RecipeMap.getByName(NetworkUtils.readStringSafe(buf)),
                (buf, map) -> NetworkUtils.writeStringSafe(buf, map != null ? map.getUnlocalizedName() : ""));

        protected long eut = 0;
        protected int duration = 0;

        protected int maxInputs = 0;
        protected int maxOutputs = 0;
        protected int maxFluidInputs = 0;
        protected int maxFluidOutputs = 0;

        protected final ItemStackHandler importItems = new ItemStackHandler(SLOTS);
        protected final ItemStackHandler exportItems = new ItemStackHandler(SLOTS);
        protected final IMultipleTankHandler importFluids = SusUtil.createTankList(SLOTS);
        protected final IMultipleTankHandler exportFluids = SusUtil.createTankList(SLOTS);

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
            this.maxFluidInputs = currentMap.getMaxFluidInputs();
            this.maxFluidOutputs = currentMap.getMaxFluidOutputs();

            this.importItems.clear();
            this.exportItems.clear();
            this.importFluids.clear();
            this.exportFluids.clear();
        }

        protected Dropdown<RecipeMap<?>, RecipeMapEntryWidget<?>> recipeMapSelector() {
            return new Dropdown<RecipeMap<?>, RecipeMapEntryWidget<?>>(recipeMapValue,
                    RecipeMapEntryWidget::getWidgetValue)
                            .values(RecipeMap.getRecipeMaps().stream()
                                    .sorted(Comparator.comparing(RecipeMap::getLocalizedName,
                                            Comparator.naturalOrder()))
                                    .collect(Collectors.toList()), RecipeMapEntryWidget::new)
                            // .setDefault(RecipeMapEntryWidget.EMPTY)
                            .interpolation(Interpolation.EXP_OUT);
        }

        protected IWidget recipeMapGui() {
            var inputItemsMatrix = Grid.mapToMatrix(ROW_LENGTH, SLOTS,
                    i -> new PhantomItemSlot().slot(importItems, i)
                            .setEnabledIf(s -> i < maxInputs));

            var inputFluidsMatrix = Grid.mapToMatrix(ROW_LENGTH, SLOTS,
                    i -> new PhantomFluidSlot().tank(importFluids, i)
                            .setEnabledIf(s -> i < maxFluidInputs));

            var outputItemsMatrix = Grid.mapToMatrix(ROW_LENGTH, SLOTS,
                    i -> new PhantomItemSlot().slot(exportItems, i)
                            .setEnabledIf(s -> i < maxOutputs));

            var outputFluidsMatrix = Grid.mapToMatrix(ROW_LENGTH, SLOTS,
                    i -> new PhantomFluidSlot().tank(exportFluids, i)
                            .setEnabledIf(s -> i < maxFluidOutputs));

            return Flow.row()
                    .setEnabledIf(s -> getCurrentMap() != null)
                    .coverChildren()
                    .collapseDisabledChild()
                    .background(GuiTextures.MC_BACKGROUND)
                    .padding(8)
                    .childPadding(4)
                    .child(Flow.column()
                            .coverChildren()
                            .collapseDisabledChild()
                            .child(new Grid() // TODO)) Align right
                                    .matrix(inputItemsMatrix)
                                    .collapseDisabledChild()
                                    .coverChildren())
                            .child(new Grid()
                                    .matrix(inputFluidsMatrix)
                                    .collapseDisabledChild()
                                    .coverChildren()))
                    .childIf(BoolSupplier.TRUE, () -> new RecipeProgressWidget()
                            .dynamic(recipeMapValue)
                            .autoIncrementProgress(() -> duration)
                            // .progress(() -> () / duration) // TODO)) Dynamic progress
                            .size(20))
                    .child(Flow.column()
                            .coverChildren()
                            .collapseDisabledChild()
                            .child(new Grid() // TODO)) Align left
                                    .matrix(outputItemsMatrix)
                                    .collapseDisabledChild()
                                    .coverChildren())
                            .child(new Grid()
                                    .matrix(outputFluidsMatrix)
                                    .collapseDisabledChild()
                                    .coverChildren()));
        }

        @Override
        public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
            syncManager.syncValue("recipeMap", recipeMapValue);

            return GTGuis.createPanel("grs_recipe_creator")
                    .child(Flow.column()
                            .coverChildren()
                            .child(recipeMapSelector())
                            .child(recipeMapGui())); // FIXME)) Fix depth order
        }

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
                if (!ServerInfo.isJeiOnServer()) {
                    return new RecipeTransferErrorTooltip(
                            I18n.format("jei.tooltip.error.recipe.transfer.no.server"));
                }

                if (!(recipeLayout.getRecipeCategory() instanceof RecipeMapCategory)) {
                    return new RecipeTransferErrorTooltip(
                            I18n.format("sussypatches.gui.recipe_creator.transfer_error.illegal_recipe"));
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
