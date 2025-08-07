package dev.tianmi.sussypatches.api.core.mixin.extension;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.common.terminal.app.prospector.widget.WidgetProspectingMap;

@MixinExtension(WidgetProspectingMap.class)
public interface ProspectingMapExtension {

    static ProspectingMapExtension cast(WidgetProspectingMap map) {
        return (ProspectingMapExtension) map;
    }

    int sus$getHoveredHeight();

    void sus$setHoveredHeight(int hoveredHeight);
}
