package dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OreDictUnifier.class, remap = false)
public abstract class OreDictUnifierMixin {

    /// @author Tian_mi
    /// @reason Feed the graph as well as the unification map, so that everything the graph owns can be rewritten in
    ///         [#RecyclingManager#init].
    @Overwrite
    public static void registerOre(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        RecyclingManager.registerOre(itemStack, materialInfo);
    }

    /// @author Tian_mi
    /// @reason [ItemMaterialInfo#getMaterial] returns `null` for an item registered without any material, and the
    ///         original code dereferences it unconditionally.
    @WrapOperation(method = "getMaterial(Lnet/minecraft/item/ItemStack;)Lgregtech/api/unification/stack/MaterialStack;",
                   at = @At(target = "Lgregtech/api/unification/stack/MaterialStack;copy()Lgregtech/api/unification/stack/MaterialStack;",
                            value = "INVOKE"))
    private static MaterialStack checkNotNull(MaterialStack ms, Operation<MaterialStack> original) {
        return ms == null ? null : original.call(ms);
    }
}
