package dev.tianmi.sussypatches.core.mixin.tweak.optimizeworldgen;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.properties.PropertyStoneType;
import net.minecraft.block.properties.PropertyHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = PropertyStoneType.class, remap = false)
public abstract class PropertyStoneTypeMixin extends PropertyHelper<StoneType> {

    @Unique
    private int sus$hashCode;

    @Unique
    private boolean sus$hashCodeInitiated = false;

    @SuppressWarnings("DataFlowIssue")
    PropertyStoneTypeMixin() { // Dummy
        super(null, null);
    }

    @WrapMethod(method = "hashCode")
    private int cachesHashCode(Operation<Integer> method) {
        if (!this.sus$hashCodeInitiated) {
            this.sus$hashCode = method.call();
            this.sus$hashCodeInitiated = true;
        }
        return sus$hashCode;
    }
}
