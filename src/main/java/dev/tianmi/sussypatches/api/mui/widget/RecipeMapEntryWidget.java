package dev.tianmi.sussypatches.api.mui.widget;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.init.Blocks;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widget.SingleChildWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.recipes.RecipeMap;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import mcp.MethodsReturnNonnullByDefault;

@ExtensionMethod(SusUtil.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeMapEntryWidget<W extends RecipeMapEntryWidget<W>> extends SingleChildWidget<W>
                                 implements IValueWidget<RecipeMap<?>> {

    public static final RecipeMapEntryWidget<?> EMPTY = new RecipeMapEntryWidget<>(null);

    @Nullable
    @Getter(onMethod_ = @Override)
    protected final RecipeMap<?> widgetValue;

    public RecipeMapEntryWidget(@Nullable RecipeMap<?> recipeMap) {
        this.widgetValue = recipeMap;
    }

    @Override
    @SuppressWarnings({ "deprecation", "DataFlowIssue" })
    public void onInit() {
        super.onInit();

        IDrawable icon;
        String key;
        if (widgetValue != null) {
            icon = widgetValue.getCatalystIcon();
            key = widgetValue.getTranslationKey();
        } else {
            icon = new ItemDrawable(Blocks.BARRIER);
            key = "sussypatches.gui.recipe_creator.unselected";
        }

        this.full().child(Flow.row()
                .full()
                .child(icon.asWidget()
                        .size(16)
                        .margin(2))
                .child(new FixedScrollingTextWidget(IKey.lang(key))
                        .color(Color.WHITE.main)
                        .shadow(true)
                        .expanded() // TODO: Fix vertical alignment
                        .marginLeft(1)
                        .marginRight(2))); // TODO: Check stencil
    }
}
