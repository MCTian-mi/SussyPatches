package dev.tianmi.sussypatches.core.mixin.api.statechangenotifier;


public interface WorldAccessor {

    default boolean invokeIsChunkLoaded(int x, int z, boolean allowEmpty) {
        return false;
    }
}
