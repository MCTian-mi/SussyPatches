package dev.tianmi.sussypatches.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/// A marker interface for Mixins whose contents are already implemented in-dev.
/// Mixins marked with this @interface should and will be removed
/// in the (highly hypothetical) next CEu update.
@Target(ElementType.TYPE)
public @interface Implemented {

    /// The target link where the Mixin contents are implemented
    String[] in() default {};
}
