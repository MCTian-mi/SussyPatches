package dev.tianmi.sussypatches.common.helper;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.api.recipe.property.StoichiometryProperty;
import dev.tianmi.sussypatches.api.unification.SusMaterialFlags;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import gregtech.integration.groovy.GroovyScriptModule;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.unification.OreDictUnifier.getMaterial;
import static gregtech.api.unification.OreDictUnifier.getMaterialInfo;
import static gregtech.api.unification.OreDictUnifier.getPrefix;

/**
 * Debug-only stoichiometry verifier to ensure scripted recipes conserve elements.
 */
public final class StoichiometryVerifier {

    private StoichiometryVerifier() {}

    public static void verifyOnBuild(RecipeBuilder<?> builder, RecipeMap<?> recipeMap) {
        Recipe recipe = builder.copy().build().getResult();
        verify(recipe, recipeMap);
    }

    public static void verify(@Nullable Recipe recipe, @Nullable RecipeMap<?> recipeMap) {
        if (recipe == null || recipeMap == null) return;
        if (!shouldVerify(recipeMap, recipe)) return;
        StoichiometryProperty.Settings settings = recipe.getProperty(StoichiometryProperty.getInstance(),
                StoichiometryProperty.DEFAULT_SETTINGS);
        if (settings.disableVerifier()) return;
        StoichiometryCheck check = new StoichiometryCheck(recipeMap, recipe, settings.lossy());
        check.run();
    }

    private static boolean shouldVerify(RecipeMap<?> map, Recipe recipe) {
        if (!SusConfig.DEBUG.enableStoichiometryVerifier) return false;
        if (!recipe.isGroovyRecipe() && !GroovyScriptModule.isCurrentlyRunning()) return false;
        String name = map.unlocalizedName;
        for (String allowed : SusConfig.DEBUG.stoichiometryRecipeMaps) {
            if (allowed.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private static void mergeBounds(Map<Element, Bounds> totals, Map<Element, Bounds> addition) {
        addition.forEach((element, bounds) ->
                totals.merge(element, bounds, Bounds::combine));
    }

    private static final class StoichiometryCheck {

        private final RecipeMap<?> map;
        private final Recipe recipe;
        private final boolean lossy;
        private final MaterialGraph graph;

        StoichiometryCheck(RecipeMap<?> map, Recipe recipe, boolean lossy) {
            this.map = map;
            this.recipe = recipe;
            this.lossy = lossy;
            this.graph = MaterialGraph.build();
        }

        void run() {
            Map<Element, Bounds> input = aggregateInputBounds(recipe.getInputs(), recipe.getFluidInputs());
            Map<Element, Bounds> output = aggregateOutputBounds(recipe.getOutputs(), recipe.getFluidOutputs());

            List<ElementViolation> violations = diff(input, output);
            if (violations.isEmpty()) return;

            String message = format(violations);
            SussyPatches.LOGGER.error(message);
            if (SusConfig.DEBUG.stoichiometryThrowOnViolation) {
                throw new StoichiometryViolationException("Stoichiometry errors were found. Stopping game");
            }
        }

        private Map<Element, Bounds> aggregateInputBounds(List<GTRecipeInput> itemInputs,
                                                          List<GTRecipeInput> fluidInputs) {
            Map<Element, Bounds> totals = new HashMap<>();
            for (GTRecipeInput input : itemInputs) {
                mergeBounds(totals, decomposeItemInput(input));
            }
            for (GTRecipeInput input : fluidInputs) {
                mergeBounds(totals, decomposeFluidInput(input));
            }
            return totals;
        }

        private Map<Element, Bounds> aggregateOutputBounds(List<ItemStack> itemOutputs,
                                                           List<FluidStack> fluidOutputs) {
            Map<Element, Bounds> totals = new HashMap<>();
            for (ItemStack stack : itemOutputs) {
                mergeBounds(totals, decomposeItemStack(stack));
            }
            for (FluidStack stack : fluidOutputs) {
                if (stack != null && stack.amount > 0) {
                    mergeBounds(totals, decomposeFluidStack(stack));
                }
            }
            return totals;
        }

        private Map<Element, Bounds> decomposeItemInput(GTRecipeInput input) {
            if (input.isNonConsumable() || !(input instanceof GTRecipeItemInput itemInput)) {
                return Map.of();
            }
            ItemStack[] alternatives = itemInput.getInputStacks();
            if (alternatives.length == 0) {
                return Map.of();
            }
            List<Map<Element, Bounds>> altBounds = new ArrayList<>(alternatives.length);
            for (ItemStack stack : alternatives) {
                if (!stack.isEmpty()) {
                    altBounds.add(decomposeItemStack(stack));
                }
            }
            return combineAlternatives(altBounds);
        }

        private Map<Element, Bounds> decomposeFluidInput(GTRecipeInput input) {
            if (input.isNonConsumable() || !(input instanceof GTRecipeFluidInput fluidInput)) {
                return Map.of();
            }
            FluidStack stack = fluidInput.getInputFluidStack();
            if (stack == null || stack.amount <= 0) {
                return Map.of();
            }
            return decomposeFluidStack(stack);
        }

        private Map<Element, Bounds> decomposeItemStack(ItemStack stack) {
            if (stack.isEmpty()) {
                return Map.of();
            }
            Map<Element, Bounds> result = new HashMap<>();
            MaterialStack materialStack = getMaterial(stack);
            if (materialStack != null) {
                mergeBounds(result, decomposeMaterial(materialStack.material, materialStack.amount));
                return result;
            }
            ItemMaterialInfo info = getMaterialInfo(stack);
            if (info != null) {
                for (MaterialStack component : info.getMaterials()) {
                    mergeBounds(result, decomposeMaterial(component.material, component.amount));
                }
                return result;
            }
            OrePrefix prefix = getPrefix(stack);
            if (prefix != null) {
                Material material = prefix.materialType;
                if (material != null) {
                    long amount = prefix.getMaterialAmount(material) * stack.getCount();
                    mergeBounds(result, decomposeMaterial(material, amount));
                }
            }
            return result;
        }

        private Map<Element, Bounds> decomposeFluidStack(FluidStack stack) {
            Material material = StoichiometryUtil.getMaterialFromFluid(stack);
            if (material == null) {
                return Map.of();
            }
            return decomposeMaterial(material, stack.amount);
        }

        private Map<Element, Bounds> decomposeMaterial(Material material, long amount) {
            if (material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                return Map.of();
            }
            return graph.resolve(material, amount);
        }

        private Map<Element, Bounds> combineAlternatives(List<Map<Element, Bounds>> alternatives) {
            if (alternatives.isEmpty()) {
                return Map.of();
            }
            Set<Element> elements = new HashSet<>();
            for (Map<Element, Bounds> bounds : alternatives) {
                elements.addAll(bounds.keySet());
            }
            Map<Element, Bounds> combined = new HashMap<>();
            for (Element element : elements) {
                long min = Long.MAX_VALUE;
                long max = Long.MIN_VALUE;
                for (Map<Element, Bounds> bounds : alternatives) {
                    Bounds current = bounds.getOrDefault(element, Bounds.zero());
                    min = Math.min(min, current.min);
                    max = Math.max(max, current.max);
                }
                if (min == Long.MAX_VALUE) {
                    min = 0;
                }
                if (max == Long.MIN_VALUE) {
                    max = 0;
                }
                combined.put(element, new Bounds(min, max));
            }
            return combined;
        }



        private List<ElementViolation> diff(Map<Element, Bounds> inputs, Map<Element, Bounds> outputs) {
            Set<Element> elements = new HashSet<>(inputs.keySet());
            elements.addAll(outputs.keySet());
            List<ElementViolation> violations = new ArrayList<>();
            for (Element element : elements) {
                Bounds in = inputs.getOrDefault(element, Bounds.zero());
                Bounds out = outputs.getOrDefault(element, Bounds.zero());
                if (out.max < in.min) {
                    if (!lossy) {
                        violations.add(new ElementViolation(element, in, out));
                    }
                } else if (out.min > in.max) {
                    violations.add(new ElementViolation(element, in, out));
                }
            }
            return violations;
        }

        private String format(List<ElementViolation> violations) {
            String elementDetails = violations.stream()
                    .map(v -> String.format(Locale.ROOT, "  - %s: inputs %s -> outputs %s",
                            v.element.getSymbol(), v.input, v.output))
                    .collect(Collectors.joining("\n"));

            return "Stoichiometry violation detected in recipe map '" + map.unlocalizedName + "'\n" +
                    elementDetails + "\nRecipe: " + recipe;
        }
    }

    private record ElementViolation(Element element, Bounds input, Bounds output) {}

    private record Bounds(long min, long max) {

        static Bounds zero() {
            return new Bounds(0, 0);
        }

        static Bounds combine(Bounds a, Bounds b) {
            return new Bounds(a.min + b.min, a.max + b.max);
        }

        Bounds scale(long factor) {
            if (factor == 1) {
                return this;
            }
            return new Bounds(min * factor, max * factor);
        }
    }

    private static final class MaterialGraph {

        private final Map<Material, Node> nodes;
        private final Map<Material, Map<Element, Bounds>> cache = new HashMap<>();

        private MaterialGraph(Map<Material, Node> nodes) {
            this.nodes = nodes;
        }

        static MaterialGraph build() {
            Map<Material, Node> nodes = new HashMap<>();
            for (Material material : getRegisteredMaterials()) {
                nodes.put(material, new Node(material));
            }
            return new MaterialGraph(nodes);
        }

        Map<Element, Bounds> resolve(Material material, long amount) {
            if (amount <= 0) {
                return Map.of();
            }
            Map<Element, Bounds> perUnit = resolvePerUnit(material, new HashSet<>());
            if (perUnit.isEmpty()) {
                return perUnit;
            }
            Map<Element, Bounds> scaled = new HashMap<>();
            perUnit.forEach((element, bounds) -> scaled.put(element, bounds.scale(amount)));
            return scaled;
        }

        private Map<Element, Bounds> resolvePerUnit(Material material, Set<Material> visiting) {
            Map<Element, Bounds> cached = cache.get(material);
            if (cached != null) {
                return cached;
            }
            if (!visiting.add(material)) {
                return Map.of();
            }

            Node node = nodes.get(material);
            if (node == null) {
                visiting.remove(material);
                return Map.of();
            }

            Map<Element, Bounds> totals = new HashMap<>(node.getDirectElementBounds());
            for (MaterialStack component : node.getComponents()) {
                Material child = component.material;
                if (child.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                    continue;
                }
                Map<Element, Bounds> childBounds = resolvePerUnit(child, visiting);
                if (childBounds.isEmpty()) {
                    continue;
                }
                Map<Element, Bounds> scaled = new HashMap<>();
                childBounds.forEach((element, bounds) ->
                        scaled.put(element, bounds.scale(component.amount)));
                mergeBounds(totals, scaled);
            }

            visiting.remove(material);
            cache.put(material, totals);
            return totals;
        }
    }

    private static final class Node {

        private final Material material;
        private final Map<Element, Bounds> directElementBounds;
        private final ImmutableList<MaterialStack> components;

        Node(Material material) {
            this.material = material;
            this.directElementBounds = computeDirect(material);
            this.components = material.getMaterialComponents();
        }

        Map<Element, Bounds> getDirectElementBounds() {
            return directElementBounds;
        }

        ImmutableList<MaterialStack> getComponents() {
            return components;
        }

        private Map<Element, Bounds> computeDirect(Material material) {
            ImmutableList<MaterialStack> components = material.getMaterialComponents();
            if (components.isEmpty()) {
                Element element = material.getElement();
                if (element == null) return Map.of();
                return Map.of(element, new Bounds(1, 1));
            }
            Map<Element, Bounds> bounds = new HashMap<>();
            for (MaterialStack stack : components) {
                Material component = stack.material;
                long amount = stack.amount;
                Element element = component.getElement();
                if (element != null) {
                    bounds.merge(element, new Bounds(amount, amount), Bounds::combine);
                }
            }
            return bounds;
        }
    }

    private static Collection<Material> getRegisteredMaterials() {
        IMaterialRegistryManager manager = MaterialRegistryManager.getInstance();
        return manager.getRegisteredMaterials();
    }

    private static class StoichiometryViolationException extends RuntimeException {
        StoichiometryViolationException(String message) {
            super(message);
        }
    }
}
