package dev.tianmi.sussypatches.api.mui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;

public interface SusGuis {

    static ModularPanel overlayPanel() {
        return overlayPanel("overlay_panel");
    }

    static ModularPanel overlayPanel(String name) {
        return ModularPanel.defaultPanel(name).full()
                .background(IDrawable.EMPTY);
    }
}
