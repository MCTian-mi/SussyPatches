package dev.tianmi.sussypatches.api.annotation;

import dev.tianmi.sussypatches.api.util.SusMods;
import gregtech.api.util.Mods;

/// A marker @interface for marking the target compat mod
public @interface Compat {

    // Target mod
    Mods[] mod() default {};

    // Target mod too but in a :sus: form
    SusMods[] mods() default {};
}
