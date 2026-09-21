package dev.tianmi.sussypatches.integration.grs;

import com.cleanroommc.groovyscript.api.IIngredient;
import dev.tianmi.sussypatches.api.recycling.Recyclable;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// The only place where GroovyScript's ingredient types meet the [Recyclable]s [RecyclingManager] works with.
///
/// @implNote This has to stay out of `api.recycling` and [RecyclingManager]: those run on every cold start, including
///           on packs without GroovyScript installed, and a `Class` constant of a GrS only type would degrade that
///           boot into a [NoClassDefFoundError]. Reached from [SusRecyclingGroovyContainer], which is only ever
///           constructed by `GrSModule` while GroovyScript is present.
@NullMarked
public final class GrsRecyclingAdapter {

    private GrsRecyclingAdapter() {
    }

    /// `IIngredient` -> [Recyclable], resolved through its first matching stack.
    ///
    /// @return `null` when the ingredient matches nothing the recycling graph can hold, i.e. never a valid node.
    public static @Nullable Recyclable fromIngredient(@Nullable IIngredient ingredient) {
        if (ingredient == null) return null;

        ItemStack[] inputStacks = ingredient.getMatchingStacks();
        if (inputStacks == null || inputStacks.length == 0) return null;

        return Recyclable.from(inputStacks[0]);
    }

    /// Ported from Susy's `RecyclingManager#addRecyclingGroovy`: derives the recycling data of `output` from the
    /// ingredients a script declared, in the same way CEu's assembling does.
    public static void addRecycling(ItemStack output, List<IIngredient> ingredients) {
        Object2IntMap<Recyclable> collected = new Object2IntOpenHashMap<>();

        for (@Nullable IIngredient input : ingredients) {
            if (input == null) continue;

            ItemStack[] inputStacks = input.getMatchingStacks();
            if (inputStacks == null || inputStacks.length == 0) continue;

            ItemStack inputStack = inputStacks[0];

            Recyclable ing = Recyclable.from(inputStack);
            if (ing == null) continue;

            int count = input.getAmount() * ing.count(inputStack);
            collected.put(ing, collected.getOrDefault(ing, 0) + count);

            // Reset the amount, since Groovyscript is weird
            input.setAmount(1);
            input.getMatchingStacks(); // This has side effects!
        }

        RecyclingManager.registerRecyclingInternal(output, output.getCount(), collected, true, true);
    }
}
