package dev.tianmi.sussypatches.api.util;

/// Helper constants for integrations w/ Fluidlogged-API
public final class FluidStateFlags {
    /// Force removing any old fluid state
    public static final int UPDATE_FLUID_STATE = 0b0100000;
    /// Keeps old fluid state (skip checks)
    public static final int KEEP_FLUID_STATE = 0b1000000;
}
