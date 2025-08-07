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
}
