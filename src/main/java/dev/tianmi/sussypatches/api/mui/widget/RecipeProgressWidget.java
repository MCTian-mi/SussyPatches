package dev.tianmi.sussypatches.api.mui.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widgets.ProgressWidget;

import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.integration.IntegrationModule;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.recipe.RecipeMapCategory;
import gregtech.modules.GregTechModules;
import lombok.experimental.ExtensionMethod;
import mcp.MethodsReturnNonnullByDefault;

@ExtensionMethod(SusUtil.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeProgressWidget extends ProgressWidget implements Interactable, IValueWidget<RecipeMap<?>> {

    private IValue<RecipeMap<?>> recipeMap;

    @Override
    public RecipeMap<?> getWidgetValue() {
        return recipeMap.getValue();
    }

    public RecipeProgressWidget recipeMap(IValue<RecipeMap<?>> recipeMap) {
        this.recipeMap = recipeMap;
        if (GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI)) {
            tooltip(t -> t.addLine(IKey.lang("gui.widget.recipeProgressWidget.default_tooltip")));
        }
        return this;
    }

    @Override
    public Result onMousePressed(int mouseButton) {
        var recipeMap = this.recipeMap.getValue();
        if (recipeMap == null) {
            return Result.IGNORE;
        }
        if (mouseButton == 0 || mouseButton == 1) {
            if (!GregTechAPI.moduleManager.isModuleEnabled(GregTechModules.MODULE_JEI)) {
                return Result.ACCEPT;
            }

            Collection<RecipeMapCategory> categories = RecipeMapCategory.getCategoriesFor(recipeMap);
            if (categories != null && !categories.isEmpty()) {
                List<String> categoryID = new ArrayList<>();
                if (recipeMap == RecipeMaps.FURNACE_RECIPES) {
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
}
