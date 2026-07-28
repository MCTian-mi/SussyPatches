package dev.tianmi.sussypatches.core.mixin.tweak.passivestructurechecking;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.tianmi.sussypatches.api.capability.IMultiblockStateManager;
import dev.tianmi.sussypatches.api.capability.SusCapabilities;
import dev.tianmi.sussypatches.api.core.mixin.extension.PassiveStructureCheckExtension;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.BlockPattern;
import lombok.val;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MultiblockControllerBase.class, remap = false)
public abstract class MultiblockControllerBaseMixin extends MetaTileEntity implements PassiveStructureCheckExtension {

    @Shadow
    @Nullable
    public BlockPattern structurePattern;
    @Unique
    private boolean sus$structureDirty;

    private MultiblockControllerBaseMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public abstract boolean isStructureFormed();

    @Shadow
    public abstract void checkStructurePattern();

    @Unique
    @Override
    public void sus$markStructureDirty() {
        this.sus$structureDirty = true;
    }

    @Inject(method = "update",
            at = @At(value = "INVOKE",
                     target = "Lgregtech/api/metatileentity/MetaTileEntity;update()V",
                     shift = At.Shift.AFTER))
    @SuppressWarnings("ConstantValue")
    private void sus$checkChangedStructure(CallbackInfo ci) {
        val registry = sus$getRegistry();
        if (registry == null) return;

        if (!isStructureFormed() ||
                (sus$structureDirty && registry.canCheck((MultiblockControllerBase) (Object) this))) {
            checkStructurePattern();
        }
    }

    @WrapWithCondition(method = "update",
                       at = @At(value = "INVOKE",
                                target = "Lgregtech/api/metatileentity/multiblock/MultiblockControllerBase;checkStructurePattern()V"))
    private boolean sus$guardPeriodicStructureCheck(MultiblockControllerBase controller) {
        // Fall back to GTCE's periodic check if the server-world capability is unexpectedly unavailable.
        return sus$getRegistry() == null;
    }

    @Inject(method = "checkStructurePattern", at = @At("HEAD"))
    private void sus$unregisterBeforeStructureCheck(CallbackInfo ci) {
        this.sus$structureDirty = false;
        val registry = sus$getRegistry();
        if (registry != null) registry.unregister((MultiblockControllerBase) (Object) this);
    }

    @Inject(method = "checkStructurePattern", at = @At("RETURN"))
    private void sus$registerAfterStructureCheck(CallbackInfo ci) {
        if (!isStructureFormed() || structurePattern == null) return;
        val registry = sus$getRegistry();
        if (registry != null) {
            registry.replaceRegistration((MultiblockControllerBase) (Object) this, structurePattern.cache.keySet());
        }
    }

    @Inject(method = "invalidateStructure", at = @At("RETURN"))
    private void sus$unregisterInvalidStructure(CallbackInfo ci) {
        this.sus$structureDirty = false;
        val registry = sus$getRegistry();
        if (registry != null) registry.unregister((MultiblockControllerBase) (Object) this);
    }

    @Unique
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    private IMultiblockStateManager sus$getRegistry() {
        return getWorld() == null ? null : getWorld().getCapability(SusCapabilities.MULTIBLOCK_STATE_MANAGER, null);
    }
}
