package dev.tianmi.sussypatches.api.util;

@FunctionalInterface
public interface BoolSupplier {

    BoolSupplier TRUE = () -> true;
    BoolSupplier FALSE = () -> false;

    boolean get();

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
