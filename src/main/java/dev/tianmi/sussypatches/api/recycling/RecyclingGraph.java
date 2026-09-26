package dev.tianmi.sussypatches.api.recycling;

import com.google.common.graph.*;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import dev.tianmi.sussypatches.shadow.net.neoforged.fml.loading.toposort.TopologicalSort;
import org.apache.commons.lang3.math.Fraction;

import java.util.*;

/// A recipe-composition graph: nodes are items or bare materials, and an edge `ingredient -> output` valued
/// `Fraction` means "one `output` is worth `Fraction` units of `ingredient`".
///
/// Beyond the raw [ValueGraph] this class tracks two extra pieces of state:
///
///  * which nodes carry an *explicit* definition, as opposed to merely appearing as someone else's ingredient —
///    the two must not be confused, or the first real definition of an item would be merged as if it were an
///    alternative recipe;
///  * per-node baselines of what the graph looked like before scripts touched it, so a reload can restore exactly
///    the previous state — including "undefined" — instead of accumulating script edits.
///
/// All mutating operations keep the graph acyclic: a definition that would close a cycle (including a self loop)
/// is rejected and the previous definition is left untouched.
///
/// @param <N> the node type; [RecyclingManager] keys it by [Recyclable]
@SuppressWarnings("UnstableApiUsage")
public final class RecyclingGraph<N> {

    private final Set<N> defined = new HashSet<>();
    private final Map<N, Map<N, Fraction>> baseline = new LinkedHashMap<>();
    private final Set<N> baselineDefined = new HashSet<>();
    private MutableValueGraph<N, Fraction> graph = ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.insertion())
            .build();

    private static <N> void replaceIncoming(MutableValueGraph<N, Fraction> graph, N node,
                                            Map<N, Fraction> incoming) {
        graph.addNode(node);
        for (N ingredient : new ArrayList<>(graph.predecessors(node))) {
            graph.removeEdge(ingredient, node);
        }
        incoming.forEach((ingredient, amount) -> graph.putEdgeValue(ingredient, node, amount));
    }

    /// The live graph. Callers must treat it as read only; it is replaced (not mutated) by [#restore].
    public ValueGraph<N, Fraction> graph() {
        return graph;
    }

    /// @return whether `node` has an explicit definition, i.e. a recipe or material list of its own — as opposed
    ///         to only being referenced as an ingredient of other nodes
    public boolean isDefined(N node) {
        return defined.contains(node);
    }

    /// Defines (or redefines) the composition of `output`.
    ///
    /// @param ingredients the new composition, `ingredient -> units per one output`; non-positive entries are
    ///                    dropped
    /// @param force       `true` replaces the previous definition outright; `false` merges it the way CEu merges
    ///                    alternative recipes — only ingredients both definitions share survive, at the smaller
    ///                    amount. An explicitly empty previous definition therefore stays empty.
    /// @return `false` when the replacement was rejected to keep the graph acyclic — the previous definition is
    ///         left untouched; `true` when it was applied
    public boolean define(N output, Map<N, Fraction> ingredients, boolean force) {
        Map<N, Fraction> replacement = new LinkedHashMap<>();
        for (Map.Entry<N, Fraction> entry : ingredients.entrySet()) {
            if (entry.getValue().compareTo(Fraction.ZERO) > 0) {
                replacement.put(entry.getKey(), entry.getValue());
            }
        }
        if (!force && defined.contains(output)) {
            replacement.keySet().retainAll(graph.predecessors(output));
            replacement.replaceAll((ingredient, amount) -> {
                Fraction previous = graph.edgeValueOrDefault(ingredient, output, Fraction.ZERO);
                return previous.compareTo(amount) < 0 ? previous : amount;
            });
        }

        // Replacing incoming edges cannot change reachability from the output in an existing DAG.
        if (replacement.containsKey(output)) return false;
        if (!replacement.isEmpty() && graph.nodes().contains(output)) {
            Set<N> visited = new HashSet<>();
            Deque<N> queue = new ArrayDeque<>();
            queue.add(output);
            visited.add(output);
            while (!queue.isEmpty()) {
                for (N successor : graph.successors(queue.remove())) {
                    if (replacement.containsKey(successor)) return false;
                    if (visited.add(successor)) queue.add(successor);
                }
            }
        }
        replaceIncoming(graph, output, replacement);
        defined.add(output);
        return true;
    }

    /// Captures the current definition of `node` as the baseline for the next [#restore], unless one was already
    /// captured — only the state *before the first script edit of a reload cycle* is the right restore target.
    public void snapshot(N node) {
        if (baseline.containsKey(node)) return;
        Map<N, Fraction> incoming = new LinkedHashMap<>();
        if (graph.nodes().contains(node)) {
            for (N ingredient : graph.predecessors(node)) {
                incoming.put(ingredient, graph.edgeValueOrDefault(ingredient, node, Fraction.ZERO));
            }
        }
        baseline.put(node, incoming);
        if (defined.contains(node)) baselineDefined.add(node);
    }

    /// Reverts every node [#snapshot] captured to its baseline, validates that the result is still acyclic, and
    /// clears the baselines.
    ///
    /// @return the nodes whose definition changed, i.e. the set a caller has to recompute downstream of
    /// @throws dev.tianmi.sussypatches.shadow.net.neoforged.fml.loading.toposort.CyclePresentException when the restored graph is cyclic — the graph is left untouched in that case
    public Set<N> restore() {
        if (baseline.isEmpty()) return Collections.emptySet();
        MutableValueGraph<N, Fraction> restored = Graphs.copyOf(graph);
        baseline.forEach((node, incoming) -> replaceIncoming(restored, node, incoming));
        // Validate only the complete restoration; intermediate edge reversals may contain cycles.
        TopologicalSort.topologicalSort(restored, null);
        Set<N> changed = new LinkedHashSet<>(baseline.keySet());
        graph = restored;
        defined.removeAll(changed);
        defined.addAll(baselineDefined);
        baseline.clear();
        baselineDefined.clear();
        return changed;
    }
}
