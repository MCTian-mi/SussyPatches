package dev.tianmi.sussypatches.core.mixin.api.statechangenotifier;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(World.class)
public interface WorldAccessor {

    @Invoker("isChunkLoaded")
    boolean invokeIsChunkLoaded(int x, int z, boolean allowEmpty);
}
