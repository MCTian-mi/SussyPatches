package dev.tianmi.sussypatches.api.mui.widget;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IInterpolation;
import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.api.widget.IValueWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.Expandable;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import dev.tianmi.sussypatches.api.mui.widget.scroll.VanillaScrollData;
import dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator.ExpandableAccessor;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Dropdown<V, I extends IWidget> extends Expandable implements IValueWidget<V> {

    protected IValue<V> value;

    @Nullable
    protected IDrawable expandedBackground;
    protected final Function<I, V> widgetToValue;

    protected final ButtonWidget<?> buttonWidget;
    protected final ListWidget<ButtonWidget<?>, ?> listWidget;

    public Dropdown(IValue<V> value, Function<I, V> widgetToValue) {
        this.value = value;
        this.widgetToValue = widgetToValue;
        this.buttonWidget = new ButtonWidget<>();
        this.listWidget = new ListWidget<>();
    }

    @Override
    public void onInit() {
        buttonWidget.size(120, 20)
                .onMousePressed(mouseButton -> {
                    if (mouseButton == 0 || mouseButton == 1) {
                        toggle();
                        return true;
                    }
                    return false;
                });

        int scrollWidth = 10;
        listWidget.size(120 - scrollWidth, 160)
                .marginBottom(1)
                .scrollDirection(new VanillaScrollData(scrollWidth));

        this.expandedBackground(GuiTextures.MC_BACKGROUND)
                // .hoverBackground(GuiTextures.MC_BACKGROUND) // TODO)) Handle hovering check
                .background(GuiTextures.MC_BACKGROUND) // TODO)) Handle hovering check
                .padding(5)
                .excludeAreaInJei()
                .normalView(Flow.column()
                        .coverChildren()
                        .collapseDisabledChild()
                        .child(buttonWidget))
                .expandedView(Flow.column()
                        .coverChildren()
                        .collapseDisabledChild()
                        .childPadding(2)
                        .child(buttonWidget)
                        .child(new Rectangle()
                                .setColor(Color.GREY.darker(2))
                                .asWidget()
                                .height(1)
                                .widthRel(1)
                                .horizontalCenter())
                        .child(listWidget));
        super.onInit();
    }

    public Dropdown<V, I> values(Iterable<V> values, Function<V, I> widgetCreator) {
        for (V value : values) {
            listWidget.child(new ButtonWidget<>()
                    .widthRel(1)
                    .height(20)
                    .child(widgetCreator.apply(value))
                    .onMousePressed(mouseButton -> {
                        setValue(widgetCreator.apply(value));
                        toggle();
                        return true;
                    }));
        }
        return getThis();
    }

    protected void setValue(I widget) {
        buttonWidget.child(widget).scheduleResize(); // TODO)) Remove resize in future mui as it's called automatically
        value.setValue(widgetToValue.apply(widget));
    }

    public Dropdown<V, I> button(Consumer<ButtonWidget<?>> operator) {
        operator.accept(buttonWidget);
        return getThis();
    }

    public Dropdown<V, I> setDefault(I defaultWidget) {
        setValue(defaultWidget);
        return getThis();
    }

    public Dropdown<V, I> list(Consumer<ListWidget<ButtonWidget<?>, ?>> operator) {
        operator.accept(listWidget);
        return getThis();
    }

    public Dropdown<V, I> expandedBackground(IDrawable... background) {
        this.expandedBackground = IDrawable.of(background);
        return getThis();
    }

    @Nullable
    public IDrawable getExpandedBackground() {
        return this.expandedBackground;
    }

    public Dropdown<V, I> disableExpandedBackground() {
        return expandedBackground(IDrawable.NONE);
    }

    @Nullable
    @Override
    public IDrawable getBackground() {
        return ((ExpandableAccessor) this).getIsExpended() ? this.expandedBackground : super.getBackground();
    }

    @Override
    public Result onMousePressed(int mouseButton) {
        return Result.IGNORE;
    }

    @Override
    public Dropdown<V, I> getThis() {
        return this;
    }

    @Override
    public Dropdown<V, I> expanded(boolean expanded) {
        super.expanded(expanded);
        return getThis();
    }

    @Override
    public Dropdown<V, I> stencilTransform(BiConsumer<java.awt.Rectangle, Boolean> stencilTransform) {
        super.stencilTransform(stencilTransform);
        return getThis();
    }

    @Override
    public Dropdown<V, I> animationDuration(int animationDuration) {
        super.animationDuration(animationDuration);
        return getThis();
    }

    @Override
    public Dropdown<V, I> interpolation(IInterpolation interpolation) {
        super.interpolation(interpolation);
        return getThis();
    }

    @Override
    @ApiStatus.Internal
    public Dropdown<V, I> normalView(IWidget normalView) {
        super.normalView(normalView);
        return getThis();
    }

    @Override
    @ApiStatus.Internal
    public Dropdown<V, I> expandedView(IWidget expandedView) {
        super.expandedView(expandedView);
        return getThis();
    }

    @Nullable
    @Override
    public V getWidgetValue() {
        return value.getValue();
    }
}
