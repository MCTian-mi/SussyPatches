package dev.tianmi.sussypatches.api.mui.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.IntSupplier;

import javax.annotation.ParametersAreNonnullByDefault;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widgets.ProgressWidget;

import dev.tianmi.sussypatches.api.util.SusUtil;
import dev.tianmi.sussypatches.core.mixin.compat.grsrecipecreator.RecipeMapAccessor;
import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.integration.IntegrationModule;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import gregtech.modules.GregTechModules;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import mcp.MethodsReturnNonnullByDefault;

@ExtensionMethod(SusUtil.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeProgressWidget extends ProgressWidget implements Interactable, IValueWidget<RecipeMap<?>> {

    public RecipeProgressWidget() {
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI)) {
            tooltip(t -> t.addLine(IKey.lang("gui.widget.recipeProgressWidget.default_tooltip")));
        }
    }

    @Setter
    @Getter(onMethod_ = @Override)
    protected RecipeMap<?> widgetValue;

    protected int ticker = 0;
    protected int duration = 100;

    @SuppressWarnings("UnusedReturnValue")
    public RecipeProgressWidget recipeMap(RecipeMap<?> recipeMap) {
        setWidgetValue(recipeMap);
        var value = recipeMap.orElse(RecipeMaps.FURNACE_RECIPES); // FallBack to default progress icon;

        direction(switch (((RecipeMapAccessor) value).getMoveType()) {
            case VERTICAL -> Direction.UP;
            /// [MoveType#VERTICAL_INVERTED] is only used for [MetaTileEntityLockedSafe],
            /// and I'm too lazy to make a special case for it.
            case VERTICAL_DOWNWARDS, VERTICAL_INVERTED -> Direction.DOWN;
            case HORIZONTAL -> Direction.RIGHT;
            case HORIZONTAL_BACKWARDS -> Direction.LEFT;
            case CIRCULAR -> Direction.CIRCULAR_CW;
        });
        texture(value.getProgressBar(), 20);
        onInit(); // Workaround to initialize the circular texture properly.

        return this;
    }

    @Override
    public Result onMousePressed(int mouseButton) {
        if (widgetValue == null) {
            return Result.IGNORE;
        }
        if (mouseButton == 0 || mouseButton == 1) {
            if (!GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI)) {
                return Result.ACCEPT;
            }

            Collection<RecipeMapCategory> categories = RecipeMapCategory.getCategoriesFor(widgetValue);
            if (categories != null && !categories.isEmpty()) {
                List<String> categoryID = new ArrayList<>();
                if (widgetValue == RecipeMaps.FURNACE_RECIPES) {
                    categoryID.add("minecraft.smelting");
                } else {
                    for (RecipeMapCategory category : categories) {
                        categoryID.add(category.getUid());
                    }
                }

                if (JustEnoughItemsModule.jeiRuntime == null) {
                    IntegrationModule.logger.error("GTCEu JEI integration has crashed, this is not a good thing");
                    return Result.ACCEPT;
                }
                JustEnoughItemsModule.jeiRuntime.getRecipesGui().showCategories(categoryID);
                return Result.SUCCESS;
            }
        }
        return Result.IGNORE;
    }

    public RecipeProgressWidget dynamic(final IValue<RecipeMap<?>> recipeMap) {
        onUpdateListener(s -> {
            var supplied = recipeMap.getValue();
            if (this.widgetValue != supplied) {
                recipeMap(supplied);
            }
        });
        return this;
    }

    public RecipeProgressWidget autoIncrementProgress(IntSupplier durationSupplier) {
        progress(() -> {
            if (duration <= 0) return 0D;
            return ticker / (double) duration;
        });
        onUpdateListener(s -> {
            var supplied = durationSupplier.getAsInt();
            if (this.duration != supplied) {
                this.ticker = 0;
                this.duration = supplied;
            }

            if (this.duration > 0) {
                this.ticker = (ticker + 1) % durationSupplier.getAsInt();
            }
        }, true);
        return this;
    }
}
