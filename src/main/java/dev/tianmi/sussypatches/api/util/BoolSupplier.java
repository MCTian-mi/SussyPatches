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

    static BoolSupplier compact(Object... conditions) {
        BoolSupplier supplier = BoolSupplier.TRUE;
        for (var condition : conditions) {
            if (condition instanceof BoolSupplier boolSupplier) {
                supplier = supplier.and(boolSupplier);
            } else if (condition instanceof Boolean bool) {
                supplier = supplier.and(BoolSupplier.of(bool));
            } else if (condition instanceof BooleanSupplier booleanSupplier) {
                supplier = supplier.and(BoolSupplier.of(booleanSupplier));
            } else {
                throw new IllegalArgumentException("Invalid condition type: " + condition.getClass());
            }
        }
        return supplier;
    }
}
