package dev.tianmi.sussypatches.common.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.SimpleGuiFactory;
import com.cleanroommc.modularui.integration.jei.JeiRecipeTransferHandler;
import com.cleanroommc.modularui.overlay.OverlayHandler;
import com.cleanroommc.modularui.overlay.OverlayManager;
import com.cleanroommc.modularui.screen.*;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.LongValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import dev.tianmi.sussypatches.client.widget.GTFluidSlot;
import dev.tianmi.sussypatches.client.widget.SussyMui2Utils;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.ClipboardUtil;
import gregtech.integration.RecipeCompatUtil;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.transfer.RecipeTransferErrorTooltip;
import mezz.jei.util.Translator;

public class GroovyBuilder implements IGuiHolder<GuiData> {

    private static SimpleGuiFactory factory;
    private static GroovyBuilder instance;

    static {
        factory = new SimpleGuiFactory("recipe_creator", GroovyBuilder::getInstance);
    }

    public static GroovyBuilder getInstance() {
        return instance;
    }

    @SideOnly(Side.CLIENT)
    public static void onPostInit() {
        instance = new GroovyBuilder();
        OverlayManager.register(new OverlayHandler(screen -> screen instanceof GuiContainerCreative, screen -> {
            GuiContainer gui = (GuiContainer) screen;
            return new CustomModularScreen() {

                @Override
                public @NotNull ModularPanel buildUI(ModularGuiContext context) {
                    ModularPanel panel = ModularPanel
                            .defaultPanel("recipe_creator_opener", gui.getXSize(), gui.getYSize())
                            .pos(gui.getGuiLeft(), gui.getGuiTop())
                            .background(IDrawable.EMPTY);

                    panel.child(new ButtonWidget<>()
                            .bottomRel(-0.36f)
                            .width(84)
                            .overlay(IKey.lang("sussypatches.gui.recipe_creator.open"))
                            .onMousePressed(mouseButton -> {
                                GuiManager.openFromClient(factory, new GuiData(Minecraft.getMinecraft().player));
                                return true;
                            }));
                    return panel;
                }

                @Override
                public void onResize(int width, int height) {
                    getMainPanel().pos(gui.getGuiLeft(), gui.getGuiTop())
                            .size(gui.getXSize(), gui.getYSize());
                    super.onResize(width, height);
                }
            };
        }));
        instance.initializeInventory();
    }

    private RecipeMap<?> map;
    private long EUt;
    private int duration;

    protected IItemHandlerModifiable importItems;
    protected IItemHandlerModifiable exportItems;
    protected FluidTankList importFluids;
    protected FluidTankList exportFluids;

    protected void initializeInventory() {
        this.importItems = new ItemStackHandler(16);
        FluidTank[] fluidImports = new FluidTank[8];
        for (int i = 0; i < fluidImports.length; i++) {
            FluidTank filteredFluidHandler = new FluidTank(
                    256000);
            fluidImports[i] = filteredFluidHandler;
        }
        this.importFluids = new FluidTankList(false, fluidImports);

        this.exportItems = new ItemStackHandler(16);
        FluidTank[] fluidExports = new FluidTank[8];
        for (int i = 0; i < fluidImports.length; i++) {
            FluidTank filteredFluidHandler = new FluidTank(
                    256000);
            fluidExports[i] = filteredFluidHandler;
        }
        this.exportFluids = new FluidTankList(false, fluidExports);
    }

    public void setEUt(long EUt) {
        this.EUt = EUt;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ModularPanel buildUI(GuiData data, PanelSyncManager syncManager, UISettings settings) {
        // We need a sync manager to deal with fluids. It's not necessarily helpful, but it's required.
        IPanelHandler recipeMapSelector = syncManager.panel("recipe_creator",
                createMapsPopup(), true);
        return ModularPanel.defaultPanel("recipe_creator", 310, 300)
                .child(Flow.row()
                        .widthRel(1.0f)
                        .margin(5)
                        .coverChildrenHeight()
                        .child(new ButtonWidget<>()
                                .marginRight(4)
                                .size(16)
                                .onMousePressed(mouse -> {
                                    if (recipeMapSelector.isPanelOpen()) {
                                        recipeMapSelector.closePanel();
                                    } else {
                                        recipeMapSelector.openPanel();
                                    }
                                    return true;
                                })
                                .addTooltipLine(IKey.lang("sussypatches.gui.recipe_creator.recipe_map_popup_button")))
                        .child(IKey
                                .lang(() -> map == null ? "sussypatches.gui.recipe_creator.unselected" :
                                        map.getUnlocalizedName())
                                .asWidget()
                                .alignY(0.5f)
                                .expanded()
                                .addTooltipLine(IKey.lang("sussypatches.gui.recipe_creator.selected_recipe_map"))))
                .child(Flow.column()
                        .margin(5)
                        .collapseDisabledChild()
                        .heightRel(0.08f)
                        .alignY(0.08f)
                        .coverChildrenWidth()
                        .child(IKey.lang("sussypatches.gui.recipe_creator.import_items").asWidget()
                                .alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxInputs() > 0))
                        .child(createIngredientRow(16, (i) -> new PhantomItemSlot().slot(this.importItems, i)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxInputs() > i))
                                        .setEnabledIf((w) -> this.map != null && this.map.getMaxInputs() > 0))
                        .child(IKey.lang("sussypatches.gui.recipe_creator.import_fluids").asWidget()
                                .alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidInputs() > 0))
                        .child(createIngredientRow(8, (i) -> new GTFluidSlot()
                                .syncHandler(
                                        GTFluidSlot.sync(importFluids.getTankAt(i)).drawAlwaysFull(true).phantom(true))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidInputs() > i))
                                        .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidInputs() > 0))
                        .child(IKey.lang("sussypatches.gui.recipe_creator.export_items").asWidget()
                                .alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxOutputs() > 0))
                        .child(createIngredientRow(16, (i) -> new PhantomItemSlot().slot(this.exportItems, i)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxOutputs() > i))
                                        .setEnabledIf((w) -> this.map != null && this.map.getMaxOutputs() > 0))
                        .child(IKey.lang("sussypatches.gui.recipe_creator.export_fluids").asWidget()
                                .alignX(Alignment.CenterLeft)
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidOutputs() > 0))
                        .child(createIngredientRow(8, (i) -> new GTFluidSlot()
                                .syncHandler(
                                        GTFluidSlot.sync(exportFluids.getTankAt(i)).drawAlwaysFull(true).phantom(true))
                                .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidOutputs() > i))
                                        .setEnabledIf((w) -> this.map != null && this.map.getMaxFluidOutputs() > 0))
                        .child(Flow.row()
                                .childPadding(5)
                                .child(IKey.lang("sussypatches.gui.recipe_creator.duration").asWidget())
                                .child(new TextFieldWidget().setNumbers()
                                        .value(new IntValue.Dynamic(() -> duration, this::setDuration)).width(100))
                                .setEnabledIf((w) -> this.map != null))
                        .child(Flow.row()
                                .childPadding(5)
                                .child(IKey.lang("sussypatches.gui.recipe_creator.eut").asWidget())
                                .child(new TextFieldWidget().setNumbers()
                                        .value(new LongValue.Dynamic(() -> EUt, this::setEUt)).width(100))
                                .setEnabledIf((w) -> this.map != null)))

                .child(new ButtonWidget<>().overlay(IKey.lang("sussypatches.gui.recipe_creator.copy_script"))
                        .widthRel(0.2f).onMousePressed((button) -> {
                            if (button != 0) return false;
                            ClipboardUtil.copyToClipboard(getGroovyScript());
                            return true;
                        }).setEnabledIf((w) -> this.map != null).alignY(0.68f).rightRel(0.05f))
                .child(new ButtonWidget<>().overlay(IKey.lang("sussypatches.gui.recipe_creator.clear")).widthRel(0.2f)
                        .onMousePressed((button) -> {
                            if (button != 0) return false;
                            clear();
                            return true;
                        }).setEnabledIf((w) -> this.map != null).alignY(0.6f).rightRel(0.05f))
                .bindPlayerInventory();
    }

    public String getGroovyScript() {
        StringBuilder result = new StringBuilder();
        result.append("recipemap('").append(this.map.getUnlocalizedName()).append("').recipeBuilder()");
        for (int i = 0; i < map.getMaxInputs(); i++) {
            if (this.importItems.getStackInSlot(i).isEmpty()) {
                continue;
            }
            result.append("\n\t.inputs(").append(getImportItemGroovy(this.importItems.getStackInSlot(i))).append(")");
        }
        for (int i = 0; i < map.getMaxFluidInputs(); i++) {
            if (this.importFluids.getTankAt(i).getFluid() == null ||
                    this.importFluids.getTankAt(i).getFluidAmount() == 0) {
                continue;
            }
            result.append("\n\t.fluidInputs(").append(getFluidGroovy(this.importFluids.getTankAt(i).getFluid()))
                    .append(")");
        }
        for (int i = 0; i < map.getMaxOutputs(); i++) {
            if (this.exportItems.getStackInSlot(i).isEmpty()) {
                continue;
            }
            result.append("\n\t.outputs(").append(getExportItemGroovy(this.exportItems.getStackInSlot(i))).append(")");
        }
        for (int i = 0; i < map.getMaxFluidOutputs(); i++) {
            if (this.exportFluids.getTankAt(i).getFluid() == null ||
                    this.exportFluids.getTankAt(i).getFluidAmount() == 0) {
                continue;
            }
            result.append("\n\t.fluidOutputs(").append(getFluidGroovy(this.exportFluids.getTankAt(i).getFluid()))
                    .append(")");
        }
        result.append("\n\t.duration(").append(this.duration).append(")");
        result.append("\n\t.EUt(").append(this.EUt).append(")");
        result.append("\n\t.buildAndRegister()");

        return result.toString();
    }

    private void clear() {
        for (int i = 0; i < importItems.getSlots(); i++) {
            importItems.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < exportItems.getSlots(); i++) {
            exportItems.setStackInSlot(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < importFluids.getTanks(); i++) {
            importFluids.getTankAt(i).getFluid().amount = 0;
        }
        for (int i = 0; i < exportFluids.getTanks(); i++) {
            exportFluids.getTankAt(i).getFluid().amount = 0;
        }
    }

    private String getImportItemGroovy(ItemStack stack) {
        OrePrefix orePrefix = OreDictUnifier.getPrefix(stack);
        if (orePrefix == null) {
            return getExportItemGroovy(stack);
        }
        return addAmount("ore('" + orePrefix + "')", stack.getCount());
    }

    private String getExportItemGroovy(ItemStack stack) {
        return addAmount("metaitem('" + RecipeCompatUtil.getMetaItemId(stack) + "')", stack.getCount());
    }

    private String getFluidGroovy(FluidStack stack) {
        return addAmount("fluid('" + stack.getFluid().getName() + "')", stack.amount);
    }

    private String addAmount(String string, int amount) {
        if (amount == 1) return string;
        return string + " * " + amount;
    }

    public Flow createIngredientRow(int amount, Function<Integer, IWidget> widgetSupplier) {
        Flow result = Flow.row()
                .widthRel(1.0f)
                .margin(5);
        for (int i = 0; i < amount; i++) {
            result.child(widgetSupplier.apply(i));
        }
        return result;
    }

    protected PanelSyncHandler.IPanelBuilder createMapsPopup() {
        return (syncManager, syncHandler) -> {
            List<IWidget> mapList = new ArrayList<>();
            StringValue searchValue = new StringValue("");

            TextFieldWidget textFieldWidget = new TextFieldWidget()
                    .left(2)
                    .right(2)
                    .marginTop(6)
                    .value(searchValue);

            for (RecipeMap<?> map : RecipeMap.getRecipeMaps().stream()
                    .sorted(Comparator.comparing(RecipeMap::getUnlocalizedName)).collect(Collectors.toList())) {
                String name = map.getUnlocalizedName();
                int id = RecipeMap.getRecipeMaps().indexOf(map);
                mapList.add(Flow.row()
                        .widthRel(1.0f)
                        .coverChildrenHeight()
                        .child(new ButtonWidget<>()
                                .widthRel(1.0f)
                                .onMousePressed(mouse -> {
                                    this.setRecipeMap(id);
                                    syncHandler.closePanel();
                                    return true;
                                })
                                .setEnabledIf((widget1) -> name.contains(textFieldWidget.getText()))
                                .addTooltipLine(IKey.lang("sussypatches.gui.recipe_creator.set_map"))
                                .overlay(IKey.str(name)))
                        .setEnabledIf((widget) -> name.contains(textFieldWidget.getText())));
            }

            return SussyMui2Utils.createPopupPanel("recipe_map_selector", 200, 200)
                    .child(Flow.column()
                            .margin(5)
                            .child(IKey.lang("sussypatches.gui.recipe_creator.recipe_maps")
                                    .asWidget())
                            .child(textFieldWidget)
                            .child(new ListWidget<>()
                                    .left(2)
                                    .right(2)
                                    .marginTop(6)
                                    .marginBottom(4)
                                    .expanded()
                                    .children(mapList)
                                    .collapseDisabledChild()
                                    .onUpdateListener((widget) -> {
                                        widget.getScrollData().clamp(widget.getScrollArea());
                                    })));
        };
    }

    private void setRecipeMap(int id) {
        map = getMap(id);
    }

    public RecipeMap<?> getMap(int mapId) {
        return RecipeMap.getRecipeMaps().get(mapId);
    }

    @Override
    public ModularScreen createScreen(GuiData data, ModularPanel mainPanel) {
        return new RecipeCreatorScreen(mainPanel);
    }

    public class RecipeCreatorScreen extends ModularScreen implements JeiRecipeTransferHandler {

        public RecipeCreatorScreen(ModularPanel mainPanel) {
            super(mainPanel);
        }

        @Override
        public IRecipeTransferError transferRecipe(IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate) {
            if (!(recipeLayout.getRecipeCategory() instanceof RecipeMapCategory))
                return new RecipeTransferErrorTooltip(
                        Translator.translateToLocal("sussypatches.gui.recipe_creator.transfer_error"));
            if (simulate)
                return null;
            RecipeMapCategory category = (RecipeMapCategory) recipeLayout.getRecipeCategory();
            String categoryName = category.getUid().substring(category.getUid().lastIndexOf(":") + 1);
            map = GTRecipeCategory.getByName(categoryName).getRecipeMap();
            List<IGuiIngredient<ItemStack>> guiIngredients = recipeLayout.getItemStacks().getGuiIngredients().entrySet()
                    .stream().sorted(
                            Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue).collect(Collectors.toList());
            int inputIndex = 0;
            int outputIndex = 0;
            for (IGuiIngredient<ItemStack> ingredient : guiIngredients) {
                if (ingredient.getDisplayedIngredient() == null || ingredient.getDisplayedIngredient().isEmpty())
                    continue;
                if (ingredient.isInput()) {
                    importItems.setStackInSlot(inputIndex, ingredient.getDisplayedIngredient());
                    inputIndex++;
                } else {
                    exportItems.setStackInSlot(outputIndex, ingredient.getDisplayedIngredient());
                    outputIndex++;
                }
            }

            List<IGuiIngredient<FluidStack>> fluidIngredients = recipeLayout.getFluidStacks().getGuiIngredients()
                    .entrySet().stream().sorted(
                            Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue).collect(Collectors.toList());
            inputIndex = 0;
            outputIndex = 0;
            for (IGuiIngredient<FluidStack> ingredient : fluidIngredients) {
                if (ingredient.getDisplayedIngredient() == null || ingredient.getDisplayedIngredient().amount == 0)
                    continue;
                if (ingredient.isInput()) {
                    importFluids.getTankAt(inputIndex).drain(Integer.MAX_VALUE, true);
                    importFluids.getTankAt(inputIndex).fill(ingredient.getDisplayedIngredient(), true);
                    inputIndex++;
                } else {
                    exportFluids.getTankAt(outputIndex).drain(Integer.MAX_VALUE, true);
                    exportFluids.getTankAt(outputIndex).fill(ingredient.getDisplayedIngredient(), true);
                    outputIndex++;
                }
            }

            return null;
        }
    }
}
