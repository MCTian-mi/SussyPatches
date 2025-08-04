package dev.tianmi.sussypatches.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// A marker interface for Interfaces that are used in Mixins.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface MixinExtension {

    /// The target classes for this extension
    Class<?>[] value() default {};
}
