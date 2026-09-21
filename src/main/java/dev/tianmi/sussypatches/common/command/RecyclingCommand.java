package dev.tianmi.sussypatches.common.command;

import com.google.common.graph.ValueGraph;
import dev.tianmi.sussypatches.api.recycling.Recyclable;
import dev.tianmi.sussypatches.api.util.ItemAndMeta;
import dev.tianmi.sussypatches.api.util.Result;
import dev.tianmi.sussypatches.loaders.recipes.RecyclingManager;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

/// A read only, in game view of the recycling DAG, so that a script change can be verified without digging through
/// log files.
///
/// `I[...]` is an item node, `M[...]` is a bare material node. The label on an ingredient line is how much of that
/// ingredient one output is worth, exactly as it was fed into the recipe reconstruction.
///
/// Only registered when `SusConfig.FEAT.recycling` is on, see
/// [dev.tianmi.sussypatches.SussyPatches#onServerLoad].
@NullMarked
public final class RecyclingCommand extends CommandBase {

    /// Guards against a `/recycling tree` on a hub item flooding the chat.
    private static final int MAX_LINES = 200;

    private static final String ITEM_SYNTAX = "Expected a registry name, optionally with a meta: modid:item or modid:item@meta.";

    private static void stats(ICommandSender sender) {
        ValueGraph<Recyclable, Fraction> graph = RecyclingManager.graph();
        reply(sender, "nodes=" + graph.nodes().size() + " edges=" + graph.edges().size()
                + " initialized=" + RecyclingManager.isInitialized()
                + " pending=" + RecyclingManager.pendingCount());
        reply(sender, "arc recipes=" + RecyclingManager.arcRecipes.size()
                + " macerator recipes=" + RecyclingManager.maceratorRecipes.size());
    }

    private static void info(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            reply(sender, "/recycling info <modid:item>[@meta]");
            return;
        }

        ItemAndMeta item = resolve(sender, args[1]);
        if (item == null) return;

        ItemStack stack = item.asStack();
        Recyclable node = Recyclable.from(stack);
        ValueGraph<Recyclable, Fraction> graph = RecyclingManager.graph();
        boolean present = graph.nodes().contains(node);

        reply(sender, node + (present ? "" : " [not in the graph]"));

        ItemMaterialInfo materialInfo = OreDictUnifier.getMaterialInfo(stack);
        if (materialInfo == null) {
            reply(sender, "  recycling data: none");
        } else {
            List<String> materials = new ArrayList<>();
            for (MaterialStack mStack : materialInfo.getMaterials()) {
                materials.add(mStack.material.getRegistryName() + " x" + mStack.amount);
            }
            reply(sender, "  recycling data: " + String.join(", ", materials));
        }

        reply(sender, "  recipes: arc=" + yesNo(RecyclingManager.arcRecipes.containsKey(node))
                + " macerator=" + yesNo(RecyclingManager.maceratorRecipes.containsKey(node)));

        if (!present) return;

        // A predecessor is an ingredient, the label is how much of it this node is worth.
        reply(sender, "  ingredients:");
        for (Recyclable ingredient : graph.predecessors(node)) {
            reply(sender, "    x" + graph.edgeValueOrDefault(ingredient, node, Fraction.ZERO) + " " + ingredient);
        }

        // A successor consumes this node, so its own recipes are the ones a change here reaches.
        Set<Recyclable> successors = graph.successors(node);
        reply(sender, "  downstream consumers (" + successors.size() + "):");
        for (Recyclable successor : successors) {
            reply(sender, "    " + successor);
        }
    }

    private static void tree(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            reply(sender, "/recycling tree <modid:item>[@meta] [depth]");
            return;
        }

        ItemAndMeta item = resolve(sender, args[1]);
        if (item == null) return;

        Recyclable root = Recyclable.from(item.asStack());
        ValueGraph<Recyclable, Fraction> graph = RecyclingManager.graph();
        if (!graph.nodes().contains(root)) {
            reply(sender, root + " is not in the recycling graph.");
            return;
        }

        int depth = args.length >= 3 ? parseDepth(args[2]) : 3;
        List<String> lines = new ArrayList<>();
        lines.add(root.toString());

        // Breadth first, so every node is printed once even on a diamond shaped graph.
        Set<Recyclable> seen = new LinkedHashSet<>();
        seen.add(root);
        Deque<Recyclable> queue = new ArrayDeque<>();
        Deque<Integer> levels = new ArrayDeque<>();
        queue.add(root);
        levels.add(0);

        boolean truncated = false;
        while (!queue.isEmpty()) {
            Recyclable node = queue.poll();
            int level = levels.poll();
            if (level >= depth) continue;

            for (Recyclable successor : graph.successors(node)) {
                if (!seen.add(successor)) continue;
                if (lines.size() >= MAX_LINES) {
                    truncated = true;
                    break;
                }
                lines.add(pad(level + 1) + successor);
                queue.add(successor);
                levels.add(level + 1);
            }
            if (truncated) break;
        }

        reply(sender, "downstream of " + root + " (depth " + depth + ", " + (lines.size() - 1) + " node(s)):");
        for (String line : lines) {
            reply(sender, "  " + line);
        }
        if (truncated) {
            reply(sender, "  ... truncated at " + MAX_LINES + " lines");
        }
    }

    private static void list(ICommandSender sender, String[] args) {
        String filter = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : null;
        String prefix = args.length >= 3 ? args[2].toLowerCase(Locale.ROOT) : null;

        List<String> lines = new ArrayList<>();
        for (Recyclable node : RecyclingManager.graph().nodes()) {
            String line = node.toString();
            String lower = line.toLowerCase(Locale.ROOT);
            if (filter != null && !lower.contains(filter)) continue;
            if (prefix != null && !lower.startsWith(prefix)) continue;
            if (lines.size() >= MAX_LINES) break;
            lines.add(line);
        }

        reply(sender, "nodes matching '" + (args.length >= 2 ? args[1] : "") + "' (" + lines.size() + " shown):");
        for (String line : lines) {
            reply(sender, "  " + line);
        }
    }

    private static @Nullable ItemAndMeta resolve(ICommandSender sender, String arg) {
        Result<ItemAndMeta, IllegalArgumentException> result = ItemAndMeta.fromString(arg);
        if (result.isErr()) {
            reply(sender, "Unknown item '" + arg + "'. " + ITEM_SYNTAX);
            return null;
        }
        return result.unwrap();
    }

    private static int parseDepth(String arg) {
        try {
            return Math.max(1, Math.min(16, Integer.parseInt(arg)));
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    /// @implNote [String#repeat] would need the downgraded API, this is a plain loop instead.
    private static String pad(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            builder.append("  ");
        }
        return builder.toString();
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static void reply(ICommandSender sender, String message) {
        sender.sendMessage(new TextComponentString(message));
    }

    @Override
    public String getName() {
        return "recycling";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/recycling <stats|info|tree|list> [item] [depth]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (!RecyclingManager.isInitialized()) {
            reply(sender, "The recycling graph is not built yet.");
            return;
        }

        if (args.length == 0) {
            reply(sender, getUsage(sender));
            return;
        }

        switch (args[0]) {
            case "stats" -> stats(sender);
            case "info" -> info(sender, args);
            case "tree" -> tree(sender, args);
            case "list" -> list(sender, args);
            default -> reply(sender, getUsage(sender));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length <= 1) {
            return getListOfStringsMatchingLastWord(args, "stats", "info", "tree", "list");
        }
        if (args.length == 2 && !args[0].equals("stats")) {
            List<String> ids = new ArrayList<>();
            for (ResourceLocation id : Item.REGISTRY.getKeys()) {
                ids.add(id.toString());
            }
            return getListOfStringsMatchingLastWord(args, ids);
        }
        return Collections.emptyList();
    }
}
