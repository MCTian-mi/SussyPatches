package dev.tianmi.sussypatches.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dev.tianmi.sussypatches.api.util.SusMods;
import gregtech.api.util.Mods;

/// A marker @interface for marking the target compat mod
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Compat {

    // Target mod
    Mods[] mod() default {};

    // Target mod too but in a :sus: form
    SusMods[] mods() default {};
}
