package dev.tianmi.sussypatches.api.util;

import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Predicates;

import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum FilterMode {

    WHITELIST,
    BLACKLIST,
    ;

    public <T> Predicate<T> mapToFilter(String @NotNull [] rawStrings,
                                        Function<@NotNull String, @NotNull Predicate<@NotNull T>> op) {
        Predicate<T> concatenated = Predicates.alwaysFalse();
        for (String rawString : rawStrings) {
            Predicate<T> predicate = op.apply(rawString);
            concatenated = concatenated.or(predicate);
        }
        return this == WHITELIST ? concatenated : concatenated.negate();
    }
}
