package dev.tianmi.sussypatches.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// For dummy mixins that are used for [IExplicitTransformer]s
/// And for transformers for their targets
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Transformer {

    Class<?>[] clazz() default {};

    Class<?>[] target() default {};
}
