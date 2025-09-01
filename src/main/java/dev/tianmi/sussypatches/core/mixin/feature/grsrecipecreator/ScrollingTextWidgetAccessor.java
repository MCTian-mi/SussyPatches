package dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator;

import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "Mui 2.5")
@Mixin(value = ScrollingTextWidget.class, remap = false)
@SuppressWarnings("DeprecatedIsStillUsed")
public interface ScrollingTextWidgetAccessor {

    @Accessor("hovering")
    boolean getIsHovering();

    @Accessor("line")
    TextRenderer.Line getLine();

    @Accessor("scroll")
    int getScroll();

    @Invoker("checkString")
    void invokeCheckString();
}
