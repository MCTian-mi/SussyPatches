package dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling;

import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import gregtech.api.recipes.ModHandler;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModHandler.class, remap = false)
public abstract class ModHandlerMixin {

    /// Feeds the DAG with the recycling data of every shaped recipe registered with unification data, replacing CEu's
    /// own `OreDictUnifier#registerOre` call.
    ///
    /// @implNote The injected call is the last statement of the `if (withUnificationData)` branch, so cancelling it
    ///           leaves everything the recipe needs already registered.
    @Inject(method = "addShapedRecipe(ZLjava/lang/String;Lnet/minecraft/item/ItemStack;ZZ[Ljava/lang/Object;)V",
            at = @At(target = "Lnet/minecraft/item/ItemStack;getCount()I", value = "INVOKE", remap = true),
            cancellable = true)
    private static void replaceWithOurs(boolean withUnificationData, String regName, ItemStack result, boolean isNBTClearing,
                                        boolean isMirrored, Object[] recipe, CallbackInfo ci) {
        RecyclingManager.addRecycling(result, result.getCount(), recipe);
        ci.cancel();
    }
}
