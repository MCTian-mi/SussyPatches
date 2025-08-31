package dev.tianmi.sussypatches.api.mui.widget.scroll;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;
import com.cleanroommc.modularui.widget.scroll.ScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;

import dev.tianmi.sussypatches.api.mui.SusGuiTextures;

public class VanillaScrollData extends VerticalScrollData {

    protected static final int VANILLA_THICKNESS = 14;
    protected static final int VANILLA_LENGTH = 17;

    protected final int length;

    public VanillaScrollData() {
        this(false);
    }

    public VanillaScrollData(int thickness) {
        this(false, thickness, VANILLA_LENGTH);
    }

    public VanillaScrollData(boolean leftAlignment) {
        this(leftAlignment, VANILLA_THICKNESS, VANILLA_LENGTH);
    }

    public VanillaScrollData(boolean leftAlignment, int thickness, int length) {
        super(leftAlignment, thickness);
        this.length = length;
    }

    @Override
    public int getScrollBarLength(ScrollArea area) {
        return this.length;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void drawScrollBar(int x, int y, int w, int h) {
        SusGuiTextures.VANILLA_SCROLL_BAR.draw(x, y, w, h);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawScrollbar(ScrollArea area) {
        boolean isOtherActive = isOtherScrollBarActive(area, true);
        int l = this.getScrollBarLength(area);
        int x = isOnAxisStart() ? 0 : area.w() - getThickness();
        int y = 0;
        int w = getThickness();
        int h = area.height;
        GuiTextures.SLOT_ITEM.draw(x, y, w, h); // Only diff

        y = getScrollBarStart(area, l, isOtherActive);
        ScrollData data2 = getOtherScrollData(area);
        if (data2 != null && isOtherActive && data2.isOnAxisStart()) {
            y += data2.getThickness();
        }
        h = l;
        drawScrollBar(x, y, w, h);
    }
}
