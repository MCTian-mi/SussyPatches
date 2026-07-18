package dev.tianmi.sussypatches.integration.jei;

import dev.tianmi.sussypatches.common.helper.DimDisplayRegistry;
import gregtech.api.GTValues;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import gregtech.integration.jei.basic.GTOreInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@NullMarked
public class OreVeinInfoPlugin implements IRecipeRegistryPlugin {

    private static final String ORE_SPAWN_UID = GTValues.MODID + ":ore_spawn_location";
    private final Map<Integer, @UnmodifiableView List<GTOreInfo>> cache = new Int2ObjectOpenHashMap<>();

    @Nullable
    private List<GTOreInfo> wrappers;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isOreCategory(IRecipeCategory<?> recipeCategory) {
        return ORE_SPAWN_UID.equals(recipeCategory.getUid());
    }

    @Override
    public <V> @UnmodifiableView List<String> getRecipeCategoryUids(IFocus<V> focus) {
        return List.of(ORE_SPAWN_UID);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper, V> @UnmodifiableView List<T> getRecipeWrappers(
            IRecipeCategory<T> recipeCategory,
            IFocus<V> focus
    ) {
        if (!isOreCategory(recipeCategory)) return Collections.emptyList();

        // A clicked dimension display icon: resolve its veins from the live dimension registry.
        List<GTOreInfo> byDim = veinsForDisplayItem(focus.getValue());
        if (byDim != null) return (List<T>) byDim;

        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        if (!isOreCategory(recipeCategory)) return Collections.emptyList();
        return (List<T>) getWrappers();
    }

    private List<GTOreInfo> getWrappers() {
        if (wrappers == null) {
            List<OreDepositDefinition> veins = WorldGenRegistry.getOreDeposits();
            List<GTOreInfo> wrappers = new ArrayList<>(veins.size());
            for (OreDepositDefinition vein : veins) {
                wrappers.add(new GTOreInfo(vein));
            }
            this.wrappers = wrappers;
        }
        return wrappers;
    }

    @Nullable
    private @UnmodifiableView List<GTOreInfo> veinsForDisplayItem(Object focusValue) {
        if (!(focusValue instanceof ItemStack stack) || stack.isEmpty()) return null;
        Integer dim = DimDisplayRegistry.getDimension(stack);
        if (dim == null || !DimensionManager.isDimensionRegistered(dim)) return null;
        return cache.computeIfAbsent(dim, id -> {
            var provider = DimensionManager.createProviderFor(id);
            return getWrappers()
                    .stream()
                    .filter(it -> it.getDefinition().getDimensionFilter().test(provider))
                    .toList();
        });
    }
}
