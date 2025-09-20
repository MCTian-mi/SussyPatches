package dev.tianmi.sussypatches.api.mui.widget;

import net.minecraft.client.renderer.GlStateManager;

import org.jetbrains.annotations.ApiStatus;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;

import dev.tianmi.sussypatches.core.mixin.compat.grsrecipecreator.ScrollingTextWidgetAccessor;

@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "Mui 2.5")
@SuppressWarnings("DeprecatedIsStillUsed")
public class FixedScrollingTextWidget extends ScrollingTextWidget {

    protected final ScrollingTextWidgetAccessor self;

    public FixedScrollingTextWidget(IKey key) {
        super(key);
        self = (ScrollingTextWidgetAccessor) this;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        if (self.getIsHovering()) {
            self.invokeCheckString();
            TextRenderer renderer = TextRenderer.SHARED;
            renderer.setColor(getColor() != null ? getColor().getAsInt() : widgetTheme.getTextColor());
            renderer.setAlignment(getAlignment(), getArea().w() + 1, getArea().h());
            // noinspection DataFlowIssue
            renderer.setShadow(isShadow() != null ? isShadow() : widgetTheme.getTextShadow());
            renderer.setPos(getArea().getPadding().left, getArea().getPadding().top);
            renderer.setScale(getScale());
            renderer.setSimulate(false);

            int scroll = self.getScroll() % (int) (self.getLine().getWidth());
            String drawString = self.getLine().getText();
            Stencil.apply(new Area(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE), context);
            GlStateManager.translate(-scroll, 0, 0);
            renderer.drawSimple(drawString);
            GlStateManager.translate(scroll, 0, 0);
            Stencil.remove();

        } else {
            super.draw(context, widgetTheme);
        }
    }

    @Override
    public FixedScrollingTextWidget getThis() {
        return this;
    }
}
