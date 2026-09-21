package dev.tianmi.sussypatches.integration.grs;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.IScriptReloadable;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/// What scripts reach as `mods.sussypatches.recycling`.
///
/// @implNote [IScriptReloadable] extends `INamed`, so this single object is both the script facing property and the
///           reload callback: `ReloadableRegistryManager` walks the property registries of every loaded container,
///           keeps the `IScriptReloadable` ones and calls [#onReload] before the scripts of a reload run and
///           [#afterScriptLoad] after they finished. That is the whole registration — there is no explicit
///           `onReload` hook to sign up for.
@NullMarked
public final class SusRecyclingGroovyContainer implements IScriptReloadable {

    private static final Collection<String> ALIASES = Collections.singleton("recycling");

    @Override
    public Collection<String> getAliases() {
        return ALIASES;
    }

    /// Replaces the recycling data of `output`, and rebuilds it along with everything downstream of it.
    ///
    /// @param output      the item the data belongs to, which must be an item CEu left alone — i.e. an `ItemStack`
    ///                    whose `OrePrefix` is absent or has no material amount. `item('minecraft:iron_ingot')` and
    ///                    `item('minecraft:iron_block')` are CEu's own and are silently refused.
    /// @param ingredients key -> count, for one `output`. Keys go through `Recyclable#from`, so an `ItemStack`
    ///                    (`item(...)`), an `Item`/`Block`, a `MetaValueItem`, a `MaterialStack` or a **full**
    ///                    oreDict name (`'ingotIron'`, *not* `'ingot'`) all work; anything else is silently dropped.
    /// @param outputCount the number of `output`s `ingredients` are meant for
    public void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount) {
        RecyclingManager.registerRecycling(output, ingredients, outputCount);
    }

    /// Same as [#registerRecycling(ItemStack, Map, int)], but keeping the data CEu derived from other recipes when
    /// `force` is `false` instead of replacing it.
    public void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount, boolean force) {
        RecyclingManager.registerRecycling(output, ingredients, outputCount, force);
    }

    /// Derives the recycling data of `output` from the ingredients it is assembled from, like CEu does for its own
    /// assembling recipes. The GroovyScript counterpart of Susy's `addRecyclingGroovy`.
    public void addRecycling(ItemStack output, List<IIngredient> ingredients) {
        GrsRecyclingAdapter.addRecycling(output, ingredients);
    }

    /// Drops the recycling data of `output` altogether, recipes included.
    public void removeRecycling(ItemStack output) {
        RecyclingManager.removeRecycling(output);
    }

    @Override
    public void onReload() {
        RecyclingManager.onScriptReload();
    }

    @Override
    public void afterScriptLoad() {
        RecyclingManager.flush();
    }
}
