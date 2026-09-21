package dev.tianmi.sussypatches.loaders.recipes;

import com.google.common.graph.*;
import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.api.recycling.ItemRecyclable;
import dev.tianmi.sussypatches.api.recycling.MaterialRecyclable;
import dev.tianmi.sussypatches.api.recycling.Recyclable;
import dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling.OreDictUnifierAccessor;
import dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling.RecipeBuilderAccessor;
import dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling.RecyclingRecipesMixin;
import dev.tianmi.sussypatches.shadow.net.neoforged.fml.loading.toposort.CyclePresentException;
import dev.tianmi.sussypatches.shadow.net.neoforged.fml.loading.toposort.TopologicalSort;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.*;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import gregtech.loaders.recipe.RecyclingRecipes;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/// The recycling dependency graph, a DAG.
///
/// A node is a [Recyclable]; an edge `ing -> out` valued `Fraction` means "one `out` is worth `Fraction` units of
/// `ing`". Hence the predecessors of `X` are what `X` is made of, and its successors are the items whose recipes
/// consume `X` — the downstream that has to be recomputed whenever `X`'s recycling data changes.
///
/// @implNote Deliberately holds no reference to GroovyScript: everything in here runs on every cold start, including
///           on packs without GroovyScript installed, so a `Class` constant of a GrS-only type would degrade that boot
///           into a [NoClassDefFoundError]. The GrS bridge lives in `integration.grs.GrsRecyclingAdapter`.
@SuppressWarnings("UnstableApiUsage")
@NullMarked
public final class RecyclingManager {

    /// Recyclable -> the arc furnace recipe CEu built for it, so that it can be removed again.
    public static final Object2ObjectOpenHashMap<Recyclable, Recipe> arcRecipes = new Object2ObjectOpenHashMap<>();
    /// Recyclable -> the macerator recipe CEu built for it, so that it can be removed again.
    public static final Object2ObjectOpenHashMap<Recyclable, Recipe> maceratorRecipes = new Object2ObjectOpenHashMap<>();
    private static final MutableValueGraph<Recyclable, Fraction> graphStorage = ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.insertion())
            .expectedNodeCount(8192)
            .build();
    /// Nodes whose recycling data has to be rebuilt by the next [#flush].
    private static final Set<Recyclable> pending = new ObjectOpenHashSet<>();
    /// Per-node snapshot of `predecessor -> Fraction` taken right before scripts first touched that node, so that
    /// [#onScriptReload] can undo precisely what the previous script run did.
    private static final Object2ObjectOpenHashMap<Recyclable, Object2ObjectMap<Recyclable, Fraction>> scriptBaselineEdges = new Object2ObjectOpenHashMap<>();
    /// `materialUnificationInfo` entries overwritten since the last [#onScriptReload], mapped to the value they had at
    /// that moment (`null` meaning "the key did not exist yet").
    private static final Object2ObjectOpenHashMap<ItemAndMetadata, ItemMaterialInfo> scriptBaselineInfo = new Object2ObjectOpenHashMap<>();
    /// Whether the cold start pass over the whole graph already happened.
    private static boolean initialized;

    private RecyclingManager() {
    }

    /// Copies lots of CEu code here to parse a recipe properly.
    ///
    /// @see RecyclingHandler#getRecyclingIngredients(int, Object...)
    public static void addRecycling(ItemStack output, int outputCount, Object... recipe) {
        Char2IntOpenHashMap inputCountMap = new Char2IntOpenHashMap();
        Object2IntMap<Recyclable> ingredients = new Object2IntOpenHashMap<>();

        int itr = 0;
        while (recipe[itr] instanceof String s) {
            for (char c : s.toCharArray()) {
                if (ToolHelper.getToolFromSymbol(c) != null) continue; // skip tools
                inputCountMap.put(c, inputCountMap.getOrDefault(c, 0) + 1);
            }
            itr++;
        }

        char lastChar = ' ';
        for (int i = itr; i < recipe.length; i++) {
            Object ingredient = recipe[i];

            /// Track the current working ingredient symbol
            if (ingredient instanceof Character) {
                lastChar = (char) ingredient;
                continue;
            }

            /// Should never happen if the recipe is formatted correctly. In the case that it isn't, this error should be
            /// handled by an earlier method call parsing the recipe.
            if (lastChar == ' ') return;

            Recyclable ing = Recyclable.from(ingredient);
            if (ing == null) continue;

            int ingCount = inputCountMap.get(lastChar) * ing.count(ingredient);
            ingredients.put(ing, ingredients.getOrDefault(ing, 0) + ingCount);
        }

        addRecyclingInternal(output, convertIntegerMap(ingredients, outputCount), false);
        markDirtyIfLoaded(output);
    }

    /// Copies lots of CEu code here to convert [GTRecipeInput]s properly.
    ///
    /// @see RecyclingHandler#getRecyclingIngredients(List, int)
    public static void addRecycling(ItemStack output, int outputCount, List<GTRecipeInput> inputs) {
        Object2IntMap<Recyclable> ingredients = new Object2IntOpenHashMap<>();

        for (GTRecipeInput input : inputs) {
            if (input == null || input.isNonConsumable() || input instanceof GTRecipeFluidInput ||
                    input instanceof FluidCellInput) {
                continue;
            }

            ItemStack[] inputStacks = input.getInputStacks();
            if (inputStacks == null || inputStacks.length == 0) continue;

            ItemStack inputStack = inputStacks[0];

            Recyclable ing = Recyclable.from(inputStack);
            if (ing == null) continue;

            int count = input.getAmount() * ing.count(inputStack);
            ingredients.put(ing, ingredients.getOrDefault(ing, 0) + count);
        }

        addRecyclingInternal(output, convertIntegerMap(ingredients, outputCount), false);
        markDirtyIfLoaded(output);
    }

    private static Object2ObjectMap<Recyclable, Fraction> convertIntegerMap(Map<Recyclable, Integer> ingredients,
                                                                            int outputCount) {
        Object2ObjectMap<Recyclable, Fraction> converted = new Object2ObjectOpenHashMap<>();
        for (Recyclable ing : ingredients.keySet()) {
            converted.put(ing, Fraction.getFraction(ingredients.get(ing), outputCount));
        }
        return converted;
    }

    private static void addRecyclingInternal(ItemStack output, Object2ObjectMap<Recyclable, Fraction> ingredients,
                                             boolean force) {
        Recyclable out = Recyclable.from(output);
        if (out != null) addRecyclingInternal(out, ingredients, force);
    }

    private static void addRecyclingInternal(Recyclable out, Object2ObjectMap<Recyclable, Fraction> ingredients,
                                             boolean force) {
        if (graphStorage.nodes().contains(out)) { // There's already recycling data.
            Set<Recyclable> predecessors = new ObjectArraySet<>(graphStorage.predecessors(out)); // No CMEs today!
            if (force) { // The original recipe is going to get removed, so it's fine to directly replace the data.
                for (Recyclable ing : predecessors) {
                    removeEdge(ing, out);
                }
            } else { // We now need to take the minimum of the two; we don't know which recipe players will use!
                // First, find the intersection of the two sets of ingredients.
                Set<Recyclable> intersection = new ObjectArraySet<>(predecessors);
                intersection.retainAll(ingredients.keySet());
                // For each edge from an ingredient in the intersection to the output, take the minimum of the values.
                for (Recyclable ing : predecessors) {
                    if (intersection.contains(ing)) {
                        Fraction fraction = graphStorage.edgeValueOrDefault(ing, out, Fraction.ZERO);
                        Fraction newFraction = ingredients.get(ing);
                        putEdge(ing, out, fraction.compareTo(newFraction) < 0 ? fraction : newFraction);
                    } else {
                        removeEdge(ing, out); // We're deleting the edge.
                    }
                }
                return;
            }
        }
        ingredients.forEach((ing, count) -> putEdge(ing, out, count));
    }

    /// Intended for GroovyScript users.
    public static void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount) {
        registerRecycling(output, ingredients, outputCount, true, true);
    }

    /// Intended for GroovyScript users.
    public static void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount,
                                         boolean force) {
        registerRecycling(output, ingredients, outputCount, force, true);
    }

    /// @param scripted whether the call comes from a groovy script, in which case the node is snapshotted and marked
    ///                 dirty so that [#flush] rebuilds it along with everything downstream of it.
    public static void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount,
                                         boolean force, boolean scripted) {
        registerRecyclingInternal(output, outputCount, toRecyclableMap(ingredients), force, scripted);
    }

    /// Same as [#registerRecycling(ItemStack, Map, int, boolean, boolean)], but for callers which already resolved their
    /// ingredients to [Recyclable]s — i.e. the GroovyScript adapter, which has to go through `IIngredient` first.
    public static void registerRecyclingInternal(ItemStack output, int outputCount,
                                                 Object2IntMap<Recyclable> ingredients, boolean force,
                                                 boolean scripted) {
        Recyclable out = Recyclable.from(output);
        if (out == null) return;
        if (outputCount <= 0) {
            SussyPatches.LOGGER.warn("Ignoring recycling data for {} with a non-positive output count of {}", out,
                    outputCount);
            return;
        }

        // Snapshot *before* the graph is mutated, otherwise the snapshot would already hold the script's own data.
        if (scripted) snapshot(out);

        addRecyclingInternal(out, convertIntegerMap(ingredients, outputCount), force);

        if (scripted) markDirty(out);
    }

    private static Object2IntMap<Recyclable> toRecyclableMap(Map<Object, Integer> ingredients) {
        Object2IntMap<Recyclable> recyclables = new Object2IntOpenHashMap<>(ingredients.size());
        for (Map.Entry<Object, Integer> entry : ingredients.entrySet()) {
            Integer amount = entry.getValue();
            if (amount == null || amount <= 0) continue;
            Recyclable recyclable = Recyclable.from(entry.getKey());
            if (recyclable == null) continue;
            recyclables.put(recyclable, recyclables.getOrDefault(recyclable, 0) + amount);
        }
        return recyclables;
    }

    /// Called from the [OreDictUnifier#registerOre] mixin, replacing CEu's implementation of it.
    ///
    /// @implNote The unification map is written here as well, so that the mixin stays a one-line delegation and CEu's
    ///           `itemStack.isEmpty()` guard is preserved in one place only.
    public static void registerOre(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        if (itemStack.isEmpty()) return;

        Recyclable out = Recyclable.from(itemStack);
        if (out == null) return;
        materialInfo.getMaterials().forEach(ms -> {
            Recyclable ing = Recyclable.from(ms);
            if (ing != null) putEdge(ing, out, Fraction.getFraction((int) ms.amount, 1));
        });

        registerOreInternal(itemStack, materialInfo);
    }

    private static void registerOreInternal(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        if (itemStack.isEmpty()) return;
        OreDictUnifierAccessor.getUnificationInfo().put(new ItemAndMetadata(itemStack), materialInfo);
    }

    /// Called from the [RecyclingRecipes#init()] mixin, right before CEu builds its own recycling recipes.
    public static void init() {
        MinecraftForge.EVENT_BUS.post(new PreRecyclingEvent());

        // Register all unification data
        TopologicalSort.topologicalSort(graphStorage, null).forEach(RecyclingManager::registerRecyclingData);
        initialized = true;
    }

    private static void registerRecyclingData(Recyclable output) {
        if (output instanceof MaterialRecyclable) return; // Skip materials

        ItemStack outputStack = output.asStack();
        OrePrefix prefix = OreDictUnifier.getPrefix(outputStack);
        if (!isDagManaged(prefix)) return; // MetaPrefixItems are handled by ceu itself

        Object2ObjectOpenHashMap<Material, Fraction> mStacks = new Object2ObjectOpenHashMap<>();

        graphStorage.predecessors(output).forEach(
                /// Theoretically no default value is needed. However, it has to be here for CrL compat, since
                /// [MutableValueGraph#edgeValue] returns [Optional] in newer guava versions
                ing -> ing.addToMStack(mStacks, graphStorage.edgeValueOrDefault(ing, output, Fraction.ZERO)));

        if (mStacks.isEmpty()) return;

        registerOreInternal(outputStack,
                new ItemMaterialInfo(
                        mStacks.entrySet().stream().map(e -> new MaterialStack(e.getKey(), e.getValue().intValue()))
                                .sorted(Comparator.comparingLong(m -> -m.amount)).collect(Collectors.toList())));
    }

    /// Drops the recycling data of `output` entirely, so that its recipes are not rebuilt from stale data.
    ///
    /// @see #flush
    private static void removeRecyclingData(Recyclable output) {
        if (!(output instanceof ItemRecyclable itemRecyclable)) return;

        OreDictUnifierAccessor.getUnificationInfo().remove(new ItemAndMetadata(itemRecyclable.asStack()));
    }

    /// @return whether the DAG owns the recycling data of an item with `prefix`, i.e. whether CEu left it alone. This is
    ///         the exact negation of the guard in [#registerRecyclingData], so that a node is never stripped of its
    ///         recipes without being rebuilt right after.
    private static boolean isDagManaged(@Nullable OrePrefix prefix) {
        return prefix == null || prefix.getMaterialAmount(null) <= 0;
    }

    private static boolean isDagManaged(Recyclable node) {
        return node instanceof ItemRecyclable itemRecyclable &&
                isDagManaged(OreDictUnifier.getPrefix(itemRecyclable.asStack()));
    }

    /// The graph itself, so that [dev.tianmi.sussypatches.common.command.RecyclingCommand] can walk it in game.
    ///
    /// @implNote The live graph, not a copy — the caller must treat it as read only.
    public static ValueGraph<Recyclable, Fraction> graph() {
        return graphStorage;
    }

    /// @return whether [#init] already walked the whole graph, i.e. whether the DAG currently owns any recycling data
    public static boolean isInitialized() {
        return initialized;
    }

    /// @return how many nodes are still waiting for the next [#flush]; `0` right after a script run finished
    public static int pendingCount() {
        return pending.size();
    }

    /// Copies and modifies CEu code.
    ///
    /// @see RecyclingHandler#addItemStackToMaterialStacks(ItemStack, Object2LongMap, int)
    public static void addItemStackToMaterialStacks(ItemStack itemStack, Object2ObjectMap<Material, Fraction> receiver,
                                                    Fraction amount) {
        // First try to get ItemMaterialInfo
        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(itemStack);
        if (info != null) {
            for (MaterialStack ms : info.getMaterials()) {
                if (!(ms.material instanceof MarkerMaterial)) {
                    addMaterialStack(receiver, amount, ms);
                }
            }
            return;
        }

        // Then try to get a single Material (UnificationEntry needs this, for example)
        MaterialStack materialStack = OreDictUnifier.getMaterial(itemStack);
        if (materialStack != null && !(materialStack.material instanceof MarkerMaterial)) {
            addMaterialStack(receiver, amount, materialStack);
        }

        // Gather any secondary materials if this item has an OrePrefix
        OrePrefix prefix = OreDictUnifier.getPrefix(itemStack);
        if (prefix != null && !prefix.secondaryMaterials.isEmpty()) {
            for (MaterialStack ms : prefix.secondaryMaterials) {
                addMaterialStack(receiver, amount, ms);
            }
        }
    }

    /// Copies and modifies CEu code. Adds a [MaterialStack] to a map of `<Material, Quantity>`.
    ///
    /// @param receiver the map to add to
    /// @param amount   the number of items in the stack
    /// @param ms       the stack to add
    /// @see RecyclingHandler#addMaterialStack(Object2LongMap, int, MaterialStack)
    private static void addMaterialStack(Object2ObjectMap<Material, Fraction> receiver, Fraction amount,
                                         MaterialStack ms) {
        Fraction oldAmount = receiver.getOrDefault(ms.material, Fraction.ZERO);
        receiver.put(ms.material, oldAmount.add(amount.multiplyBy(Fraction.getFraction((int) ms.amount, 1))));
    }

    /// Only removes recipes from [RecipeMaps#MACERATOR_RECIPES] and [RecipeMaps#ARC_FURNACE_RECIPES].
    ///
    /// @implNote As long as [RecipeMap#removeRecipe(Recipe)] is called while a groovy script runs, CEu treats the recipe
    ///           as a GrS recipe and keeps a backup of it.
    public static void removeByRecyclable(Recyclable recyclable) {
        Recipe recipe = arcRecipes.remove(recyclable);
        if (recipe != null) {
            RecipeMaps.ARC_FURNACE_RECIPES.removeRecipe(recipe);
        }

        recipe = maceratorRecipes.remove(recyclable);
        if (recipe != null) {
            RecipeMaps.MACERATOR_RECIPES.removeRecipe(recipe);
        }
    }

    /// Intended for GroovyScript users: drops the recycling data of `output` altogether, recipes included.
    ///
    /// @implNote The edges have to go before [#markDirty] takes the snapshot, otherwise the snapshot would already hold
    ///           the cleared state and a later [#onScriptReload] would not be able to put the data back.
    public static void removeRecycling(ItemStack output) {
        if (!initialized) return;

        Recyclable out = Recyclable.from(output);
        if (out == null || !graphStorage.nodes().contains(out)) return;

        snapshot(out);
        for (Recyclable ing : new ObjectArraySet<>(graphStorage.predecessors(out))) {
            removeEdge(ing, out);
        }
        markDirty(out);
    }

    /// Mixin hook, with lots of code copied from CEu.
    ///
    /// @see RecyclingRecipesMixin
    /// @see RecipeBuilder#buildAndRegister()
    public static void buildAndRegister(RecipeBuilder<?> recipeBuilder, ItemStack output) {
        var accessor = (RecipeBuilderAccessor) recipeBuilder;

        var buildAction = accessor.getOnBuildAction();
        if (buildAction != null) {
            buildAction.accept(recipeBuilder);
        }

        ValidationResult<Recipe> validationResult = recipeBuilder.build();
        RecipeMap<?> recipeMap = accessor.getRecipeMap();
        recipeMap.addRecipe(validationResult);

        /// Extra code to register recycling recipes
        if (validationResult.getType() == EnumValidationResult.VALID) {
            Recipe recipe = validationResult.getResult();
            Recyclable recyclable = Recyclable.from(output);
            if (recyclable == null) return;

            if (recipeMap == RecipeMaps.ARC_FURNACE_RECIPES) {
                arcRecipes.put(recyclable, recipe);
            } else if (recipeMap == RecipeMaps.MACERATOR_RECIPES) {
                maceratorRecipes.put(recyclable, recipe);
            }
        }
    }

    /// Called by the GroovyScript adapter *before* the scripts of a reload are run, so that the effects of the previous
    /// run are undone and the re-run starts from the same state the previous one did.
    ///
    /// @implNote This is what makes repeated reloads idempotent: without it, a script which was edited or deleted would
    ///           leave its previous contribution behind.
    public static void onScriptReload() {
        if (scriptBaselineEdges.isEmpty() && scriptBaselineInfo.isEmpty()) return;

        for (Map.Entry<Recyclable, Object2ObjectMap<Recyclable, Fraction>> entry : scriptBaselineEdges.entrySet()) {
            Recyclable node = entry.getKey();
            Object2ObjectMap<Recyclable, Fraction> baseline = entry.getValue();

            // Drop every predecessor the script introduced, but keep the CEu derived ones it may have force-deleted —
            // that is the whole point of keeping a snapshot.
            Set<Recyclable> current = new ObjectArraySet<>(predecessorsOf(node));
            current.removeAll(baseline.keySet());
            current.forEach(ing -> removeEdge(ing, node));

            baseline.forEach((ing, fraction) -> putEdge(ing, node, fraction));
            markDirty(node);
        }

        for (Map.Entry<ItemAndMetadata, ItemMaterialInfo> entry : scriptBaselineInfo.entrySet()) {
            ItemAndMetadata key = entry.getKey();
            ItemMaterialInfo info = entry.getValue();
            if (info == null) {
                OreDictUnifierAccessor.getUnificationInfo().remove(key);
            } else {
                OreDictUnifierAccessor.getUnificationInfo().put(key, info);
            }
        }

        scriptBaselineEdges.clear();
        scriptBaselineInfo.clear();
    }

    /// Called once the game reached `FMLLoadCompleteEvent`, as a safety net for script runs which did not go through the
    /// GroovyScript reload hooks.
    public static void onLoadComplete() {
        flush();
    }

    /// Rebuilds the recycling data of everything the scripts touched, along with its downstream closure.
    ///
    /// @implNote Idempotent and safe to call at any point: it spins down to nothing when no script dirtied anything.
    public static void flush() {
        if (!initialized || pending.isEmpty()) return;

        Set<Recyclable> dirty = new ObjectOpenHashSet<>(pending);
        pending.clear();

        List<Recyclable> order;
        try {
            order = TopologicalSort.topologicalSort(inducedSubgraph(dirty), null);
        } catch (CyclePresentException e) {
            SussyPatches.LOGGER.error("Recycling graph now contains a cycle, skipping the rebuild of {} nodes!",
                    dirty.size(), e);
            return;
        }

        for (Recyclable node : order) {
            // A node CEu owns must not be stripped of its recipes, we would not put them back.
            if (!isDagManaged(node)) continue;

            snapshot(node);
            removeByRecyclable(node);
            if (graphStorage.predecessors(node).isEmpty()) {
                // The scripts deleted the data of this node, so its unification entry has to go as well.
                removeRecyclingData(node);
            } else {
                registerRecyclingData(node);
            }
            rebuildRecyclingRecipes(node);
        }
    }

    /// Marks `node` and its whole downstream closure as needing a rebuild.
    private static void markDirty(Recyclable node) {
        snapshot(node);
        pending.add(node);
        for (Recyclable successor : successorClosure(node)) {
            snapshot(successor);
            pending.add(successor);
        }
    }

    /// [#markDirty] for graph mutations which may happen after the cold start pass, i.e. recipes declared by scripts
    /// through the assembler or crafting hooks. CEu's own [`OreDictUnifier#registerOre`] call is suppressed on those
    /// paths, so without this the recycling data of such an item would never be written out at all.
    ///
    /// @implNote A no-op during the cold start pass: there [#init] walks the whole graph anyway.
    private static void markDirtyIfLoaded(ItemStack output) {
        if (!initialized) return;
        Recyclable out = Recyclable.from(output);
        if (out != null) markDirty(out);
    }

    /// Every successor of `node`, direct and indirect — i.e. every item whose recycling recipe derives from it.
    private static Set<Recyclable> successorClosure(Recyclable node) {
        Set<Recyclable> visited = new ObjectOpenHashSet<>();
        Deque<Recyclable> queue = new ArrayDeque<>();
        queue.add(node);

        while (!queue.isEmpty()) {
            for (Recyclable next : successorsOf(queue.poll())) {
                if (next.equals(node) || !visited.add(next)) continue;
                queue.add(next);
            }
        }
        return visited;
    }

    /// Records what `node` looked like before scripts got to touch it, unless that already happened in this cycle.
    private static void snapshot(Recyclable node) {
        if (scriptBaselineEdges.containsKey(node)) return;

        Object2ObjectMap<Recyclable, Fraction> edges = new Object2ObjectOpenHashMap<>();
        for (Recyclable ing : predecessorsOf(node)) {
            edges.put(ing, graphStorage.edgeValueOrDefault(ing, node, Fraction.ZERO));
        }
        scriptBaselineEdges.put(node, edges);

        if (node instanceof ItemRecyclable itemRecyclable) {
            ItemAndMetadata key = new ItemAndMetadata(itemRecyclable.asStack());
            scriptBaselineInfo.put(key, OreDictUnifierAccessor.getUnificationInfo().get(key));
        }
    }

    /// Rebuilds the macerator and arc furnace recipes of `node` from its freshly computed unification info, so that
    /// [RecipeMap#compileRecipe] does not silently reject them as duplicates.
    private static void rebuildRecyclingRecipes(Recyclable node) {
        ItemStack output = node.asStack();
        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(output);
        if (info == null) return;

        RecyclingRecipes.registerRecyclingRecipes(output, new ArrayList<>(info.getMaterials()), false, null);
    }

    /// The sub-graph spanned by `dirty`, so that the topological sort only ever walks the region the scripts touched
    /// instead of the whole (potentially multi thousand node) graph.
    private static MutableValueGraph<Recyclable, Fraction> inducedSubgraph(Set<Recyclable> dirty) {
        MutableValueGraph<Recyclable, Fraction> subgraph = ValueGraphBuilder.directed()
                .allowsSelfLoops(false)
                .nodeOrder(ElementOrder.insertion())
                .build();

        for (Recyclable node : dirty) {
            if (graphStorage.nodes().contains(node)) subgraph.addNode(node);
        }

        for (Recyclable node : new ArrayList<>(subgraph.nodes())) {
            for (Recyclable ing : graphStorage.predecessors(node)) {
                if (subgraph.nodes().contains(ing)) {
                    subgraph.putEdgeValue(ing, node, graphStorage.edgeValueOrDefault(ing, node, Fraction.ZERO));
                }
            }
        }
        return subgraph;
    }

    /// [ValueGraph#successors] and [ValueGraph#predecessors] throw for a node which the graph does not hold (yet), so
    /// neither [#snapshot] nor [#successorClosure] may assume the node already has an edge. [#registerRecycling]
    /// snapshots an item *before* its very first ingredient edge exists, and an ingredient list resolving to nothing
    /// leaves the node out of the graph entirely. A missing node simply has no neighbours.
    private static Set<Recyclable> successorsOf(Recyclable node) {
        return graphStorage.nodes().contains(node) ? graphStorage.successors(node) : Collections.emptySet();
    }

    /// @see #successorsOf
    private static Set<Recyclable> predecessorsOf(Recyclable node) {
        return graphStorage.nodes().contains(node) ? graphStorage.predecessors(node) : Collections.emptySet();
    }

    /// [MutableValueGraph#putEdgeValue] throws on a self loop, since the graph is built with `allowsSelfLoops(false)`, and
    /// a script is free to hand us one.
    private static void putEdge(Recyclable ing, Recyclable out, Fraction value) {
        if (ing.equals(out)) {
            SussyPatches.LOGGER.warn("Ignoring self loop recycling edge on {}", out);
            return;
        }
        graphStorage.putEdgeValue(ing, out, value);
    }

    /// [MutableValueGraph#removeEdge] and `Graph#hasEdgeConnecting` do not both exist in Guava 21, so the edge set itself
    /// is asked before the removal. Going through `EndpointPair` also tolerates nodes that left the graph.
    private static void removeEdge(Recyclable ing, Recyclable out) {
        if (graphStorage.edges().contains(EndpointPair.ordered(ing, out))) {
            graphStorage.removeEdge(ing, out);
        }
    }

    public static class PreRecyclingEvent extends Event {
    }
}
