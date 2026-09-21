package dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.AssemblerRecipeBuilder;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeMaps.class, remap = false)
public abstract class RecipeMapsMixin {

    /// Feeds the DAG with the recycling data of every assembler recipe asking for it, replacing CEu's own
    /// `OreDictUnifier#registerOre` call.
    ///
    /// @implNote The `false` return keeps the `if` body of the lambda from running twice. Should CEu ever be updated to
    ///           2.9 this has to be re-targeted at `lambda$static$2`.
    @WrapOperation(method = "lambda$static$1",
                   at = @At(target = "Lgregtech/api/recipes/builders/AssemblerRecipeBuilder;isWithRecycling()Z",
                            value = "INVOKE"))
    private static boolean replaceWithOurs(AssemblerRecipeBuilder builder, Operation<Boolean> original) {
        if (builder.isWithRecycling()) {
            ItemStack output = builder.getOutputs().getFirst();
            RecyclingManager.addRecycling(output, output.getCount(), builder.getInputs());
        }
        return false;
    }
}
