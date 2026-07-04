package dev.tianmi.sussypatches.api.util;

import com.google.common.base.Predicates;
import org.jspecify.annotations.NullMarked;

import java.util.function.Function;
import java.util.function.Predicate;

@NullMarked
public enum FilterMode {

    WHITELIST,
    BLACKLIST,
    ;

    public <T> Predicate<T> mapToFilter(String[] rawStrings,
                                        Function<String, Predicate<T>> op) {
        Predicate<T> concatenated = Predicates.alwaysFalse();
        for (String rawString : rawStrings) {
            Predicate<T> predicate = op.apply(rawString);
            concatenated = concatenated.or(predicate);
        }
        return this == WHITELIST ? concatenated : concatenated.negate();
    }
}
