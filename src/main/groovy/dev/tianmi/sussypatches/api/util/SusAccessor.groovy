package dev.tianmi.sussypatches.api.util

class SusAccessor {
    static <Value, Type> Value get(Type instance, String field) {
        instance."${field}"
    }

    static <Value, Type> Value invoke(Type instance, String method, Object... args) {
        instance."${method}"(args)
    }

    static <Value, Type> void set(Type instance, String field, Value value) {
        instance."${field}" = value
    }
}

