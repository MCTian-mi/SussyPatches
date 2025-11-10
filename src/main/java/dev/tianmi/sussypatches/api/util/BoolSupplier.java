package dev.tianmi.sussypatches.api.util;

import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface BoolSupplier extends BooleanSupplier {

    BoolSupplier TRUE = () -> true;
    BoolSupplier FALSE = () -> false;

    boolean get();

    @Override
    default boolean getAsBoolean() {
        return get();
    }

    static BoolSupplier of(BooleanSupplier supplier) {
        return supplier::getAsBoolean;
    }

    static BoolSupplier of(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    default BoolSupplier and(BoolSupplier other) {
        return () -> this.get() && other.get();
    }

    default BoolSupplier or(BoolSupplier other) {
        return () -> this.get() || other.get();
    }

    default BoolSupplier xor(BoolSupplier other) {
        return () -> this.get() ^ other.get();
    }

    default BoolSupplier negate() {
        return () -> !this.get();
    }

    static BoolSupplier concat(Object... conditions) {
        BoolSupplier concatenated = BoolSupplier.TRUE;
        for (var condition : conditions) {
            switch (condition) {
                case BoolSupplier boolSupplier -> concatenated = concatenated.and(boolSupplier);
                case Boolean bool -> concatenated = concatenated.and(BoolSupplier.of(bool));
                case BooleanSupplier booleanSupplier -> concatenated = concatenated.and(BoolSupplier.of(booleanSupplier));
                default -> throw new IllegalArgumentException("Invalid condition type: " + condition.getClass());
            }
        }
        return concatenated;
    }
}
