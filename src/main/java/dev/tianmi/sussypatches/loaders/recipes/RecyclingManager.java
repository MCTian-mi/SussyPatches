package dev.tianmi.sussypatches.loaders.recipes;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.api.recycling.ItemRecyclable;
import dev.tianmi.sussypatches.api.recycling.Recyclable;
import dev.tianmi.sussypatches.api.recycling.RecyclingGraph;
import dev.tianmi.sussypatches.core.mixin.tweak.dagrecycling.OreDictUnifierAccessor;
import dev.tianmi.sussypatches.shadow.net.neoforged.fml.loading.toposort.CyclePresentException;
import dev.tianmi.sussypatches.shadow.net.neoforged.fml.loading.toposort.TopologicalSort;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.FluidCellInput;
import gregtech.api.recipes.RecyclingHandler;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.loaders.recipe.RecyclingRecipes;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.NullMarked;

import java.util.*;

/// The recycling dependency graph, a DAG, plus its lifecycle.
///
/// A node is a [Recyclable]; an edge `ing -> out` valued `Fraction` means "one `out` is worth `Fraction` units of
/// `ing`". Hence the predecessors of `X` are what `X` is made of, and its successors are the items whose recipes
/// consume `X` — the downstream that has to be recomputed whenever `X`'s recycling data changes.
///
/// This class owns the graph itself and everything that feeds or schedules it. The two things the graph *produces*
/// each live in their own class: the projection into CEu's unification map in [RecyclingDataPublisher], and the
/// arc furnace / macerator recipes CEu generates from that map in [RecyclingRecipeWriter].
///
/// @implNote Deliberately holds no reference to GroovyScript: everything in here runs on every cold start, including
///           on packs without GroovyScript installed, so a `Class` constant of a GrS-only type would degrade that boot
///           into a [NoClassDefFoundError]. The GrS bridge lives in `integration.grs.GrsRecyclingAdapter`.
@SuppressWarnings("UnstableApiUsage")
@NullMarked
public final class RecyclingManager {

    /// The DAG itself, including the script-reload baselines.
    private static final RecyclingGraph<Recyclable> definitions = new RecyclingGraph<>();
    /// Nodes whose recycling data has to be rebuilt by the next [#flush].
    private static final Set<Recyclable> pending = new ObjectOpenHashSet<>();
    /// `materialUnificationInfo` entries overwritten since the last [#onScriptReload], mapped to the value they had
    /// at that moment (`null` meaning "the key did not exist yet").
    private static final Object2ObjectOpenHashMap<ItemAndMetadata, ItemMaterialInfo> scriptBaselineInfo = new Object2ObjectOpenHashMap<>();
    /// Whether the cold start pass over the whole graph already happened.
    private static boolean initialized;

    private RecyclingManager() {
    }

    // -------------------------------------------------------------------------------------------------- ingestion ---

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

            // Track the current working ingredient symbol
            if (ingredient instanceof Character) {
                lastChar = (char) ingredient;
                continue;
            }

            // Should never happen if the recipe is formatted correctly. In the case that it isn't, this error should
            // be handled by an earlier method call parsing the recipe.
            if (lastChar == ' ') return;

            Recyclable ing = Recyclable.from(ingredient);
            if (ing == null) continue;

            int ingCount = inputCountMap.get(lastChar) * ing.count(ingredient);
            ingredients.put(ing, ingredients.getOrDefault(ing, 0) + ingCount);
        }

        ingest(output, outputCount, ingredients);
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

        ingest(output, outputCount, ingredients);
    }

    /// Shared tail of the recipe-ingestion hooks: a no-op during the cold start pass (there [#init] walks the whole
    /// graph anyway), afterwards the node and its downstream are snapshotted *before* the definition is replaced, so
    /// that a script reload restores what CEu had rather than what an earlier script run left behind.
    private static void ingest(ItemStack output, int outputCount, Object2IntMap<Recyclable> ingredients) {
        markDirtyIfLoaded(output);
        Recyclable out = Recyclable.from(output);
        if (out != null) addRecyclingInternal(out, convertIntegerMap(ingredients, outputCount), false);
    }

    private static Object2ObjectMap<Recyclable, Fraction> convertIntegerMap(Map<Recyclable, Integer> ingredients,
                                                                            int outputCount) {
        Object2ObjectMap<Recyclable, Fraction> converted = new Object2ObjectOpenHashMap<>();
        for (Recyclable ing : ingredients.keySet()) {
            converted.put(ing, Fraction.getFraction(ingredients.get(ing), outputCount));
        }
        return converted;
    }

    private static void addRecyclingInternal(Recyclable out, Object2ObjectMap<Recyclable, Fraction> ingredients,
                                             boolean force) {
        if (!definitions.define(out, ingredients, force)) {
            SussyPatches.LOGGER.warn("Ignoring cyclic recycling definition for {}", out);
        }
    }

    ///
    /// @implNote The unification map is written here as well, so that the mixin stays a one-line delegation and CEu's
    ///           `itemStack.isEmpty()` guard is preserved in one place only.
    public static void registerOre(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        if (itemStack.isEmpty()) return;

        Recyclable out = Recyclable.from(itemStack);
        if (out == null) return;
        markDirtyIfLoaded(itemStack);
        Object2ObjectMap<Recyclable, Fraction> ingredients = new Object2ObjectOpenHashMap<>();
        materialInfo.getMaterials().forEach(ms -> {
            Recyclable ing = Recyclable.from(ms);
            if (ing != null) ingredients.merge(ing, Fraction.getFraction((int) ms.amount, 1), Fraction::add);
        });
        addRecyclingInternal(out, ingredients, true);

        registerOreInternal(itemStack, materialInfo);
    }

    private static void registerOreInternal(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        if (itemStack.isEmpty()) return;
        OreDictUnifierAccessor.getUnificationInfo().put(new ItemAndMetadata(itemStack), materialInfo);
    }

    // --------------------------------------------------------------------------------------------- script facing ---

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

    /// Same as [#registerRecycling(ItemStack, Map, int, boolean, boolean)], but for callers which already resolved
    /// their ingredients to [Recyclable]s — i.e. the GroovyScript adapter, which has to go through `IIngredient`
    /// first.
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
        if (!RecyclingDataPublisher.isDagManaged(out)) return;

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

    /// Intended for GroovyScript users: drops the recycling data of `output` altogether, recipes included.
    public static void removeRecycling(ItemStack output) {
        Recyclable out = Recyclable.from(output);
        if (out == null || !RecyclingDataPublisher.isDagManaged(out)) return;

        snapshot(out);
        definitions.define(out, Collections.emptyMap(), true);
        markDirty(out);
    }

    // -------------------------------------------------------------------------------------------------- lifecycle ---

    /// Called from the [RecyclingRecipes#init()] mixin, right before CEu builds its own recycling recipes.
    public static void init() {
        MinecraftForge.EVENT_BUS.post(new PreRecyclingEvent());

        // Register all unification data
        TopologicalSort.topologicalSort(definitions.graph(), null)
                .forEach(node -> RecyclingDataPublisher.publish(node, definitions));
        initialized = true;
    }

    /// Called by the GroovyScript adapter *before* the scripts of a reload are run, so that the effects of the
    /// previous run are undone and the re-run starts from the same state the previous one did.
    ///
    /// @implNote This is what makes repeated reloads idempotent: without it, a script which was edited or deleted
    ///           would leave its previous contribution behind.
    public static void onScriptReload() {
        Set<Recyclable> restored = definitions.restore();
        for (Recyclable node : restored) {
            pending.add(node);
            pending.addAll(successorClosure(node));
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

        scriptBaselineInfo.clear();
    }

    /// Called once the game reached `FMLLoadCompleteEvent`, as a safety net for script runs which did not go through
    /// the GroovyScript reload hooks.
    public static void onLoadComplete() {
        flush();
    }

    /// Rebuilds the recycling data of everything the scripts touched, along with its downstream closure.
    ///
    /// @implNote Idempotent and safe to call at any point: it spins down to nothing when no script dirtied anything.
    public static void flush() {
        if (!initialized || pending.isEmpty()) return;

        Set<Recyclable> dirty = new ObjectOpenHashSet<>(pending);

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
            if (!RecyclingDataPublisher.isDagManaged(node)) continue;

            snapshot(node);
            RecyclingRecipeWriter.removeByRecyclable(node);
            RecyclingDataPublisher.publish(node, definitions);
            RecyclingRecipeWriter.rebuild(node);
        }
        pending.removeAll(dirty);
    }

    // ---------------------------------------------------------------------------------------------- graph helpers ---

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
        definitions.snapshot(node);
        if (node instanceof ItemRecyclable itemRecyclable) {
            ItemAndMetadata key = new ItemAndMetadata(itemRecyclable.asStack());
            if (!scriptBaselineInfo.containsKey(key)) {
                scriptBaselineInfo.put(key, OreDictUnifierAccessor.getUnificationInfo().get(key));
            }
        }
    }

    /// The sub-graph spanned by `dirty`, so that the topological sort only ever walks the region the scripts touched
    /// instead of the whole (potentially multi thousand node) graph.
    private static MutableValueGraph<Recyclable, Fraction> inducedSubgraph(Set<Recyclable> dirty) {
        MutableValueGraph<Recyclable, Fraction> subgraph = ValueGraphBuilder.directed()
                .allowsSelfLoops(false)
                .nodeOrder(ElementOrder.insertion())
                .build();

        ValueGraph<Recyclable, Fraction> graph = definitions.graph();
        for (Recyclable node : dirty) {
            if (graph.nodes().contains(node)) subgraph.addNode(node);
        }

        for (Recyclable node : new ArrayList<>(subgraph.nodes())) {
            for (Recyclable ing : graph.predecessors(node)) {
                if (subgraph.nodes().contains(ing)) {
                    subgraph.putEdgeValue(ing, node, graph.edgeValueOrDefault(ing, node, Fraction.ZERO));
                }
            }
        }
        return subgraph;
    }

    /// [ValueGraph#successors] and [ValueGraph#predecessors] throw for a node which the graph does not hold (yet),
    /// so neither [#snapshot] nor [#successorClosure] may assume the node already has an edge. [#registerRecycling]
    /// snapshots an item *before* its very first ingredient edge exists, and an ingredient list resolving to nothing
    /// leaves the node out of the graph entirely. A missing node simply has no neighbours.
    private static Set<Recyclable> successorsOf(Recyclable node) {
        ValueGraph<Recyclable, Fraction> graph = definitions.graph();
        return graph.nodes().contains(node) ? graph.successors(node) : Collections.emptySet();
    }

    // ------------------------------------------------------------------------------------------------- introspection ---

    /// The graph itself, so that [dev.tianmi.sussypatches.common.command.RecyclingCommand] can walk it in game.
    ///
    /// @implNote The live graph, not a copy — the caller must treat it as read only.
    public static ValueGraph<Recyclable, Fraction> graph() {
        return definitions.graph();
    }

    /// @return whether [#init] already walked the whole graph, i.e. whether the DAG currently owns any recycling data
    public static boolean isInitialized() {
        return initialized;
    }

    /// @return how many nodes are still waiting for the next [#flush]; `0` right after a script run finished
    public static int pendingCount() {
        return pending.size();
    }

    public static class PreRecyclingEvent extends Event {
    }
}
