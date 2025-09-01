package dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.cleanroommc.modularui.widgets.Expandable;

@Mixin(value = Expandable.class, remap = false)
public interface ExpandableAccessor {

    @Accessor("expanded")
    boolean getIsExpended();
}
