package dev.tianmi.sussypatches.common.stoichiometry;

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
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import gregtech.integration.groovy.GroovyScriptModule;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.unification.OreDictUnifier.getMaterial;
import static gregtech.api.unification.OreDictUnifier.getMaterialInfo;

/**
 * Debug-only stoichiometry verifier to ensure scripted recipes conserve elements.
 */
public final class StoichiometryVerifier {
    public static final int CHANCE_DENOMINATOR = 10000;
    public static final StoichiometryState stoichiometryState = new StoichiometryState(new HashSet<>());
    private StoichiometryVerifier() {}

    private static final Map<Material, Map<Element, Bounds>> INFERRED_BOUNDS = new HashMap<>();

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

    private static void mergeUnknowns(Map<Material, Fraction> totals, Map<Material, Fraction> addition) {
        addition.forEach((material, amount) -> totals.merge(material, amount, Fraction::add));
    }

    private static Fraction reciprocal(Fraction value) {
        if (value.equals(Fraction.ZERO)) {
            throw new IllegalArgumentException("Cannot take reciprocal of zero");
        }
        return Fraction.getFraction(value.getDenominator(), value.getNumerator());
    }

    private static Fraction materialAmount(MaterialStack stack) {
        return Fraction.getFraction((int) stack.amount, 1);
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
            Aggregation input = aggregateInputBounds(recipe.getInputs(), recipe.getFluidInputs());
            Aggregation output = aggregateOutputBounds(recipe.getOutputs(), recipe.getFluidOutputs());

            processUnknowns(input, output);

            List<ElementViolation> violations = diff(input, output);
            if (violations.isEmpty()) return;

            String message = format(violations);
            SussyPatches.LOGGER.error(message);
            if (SusConfig.DEBUG.stoichiometryThrowOnViolation) {
                throw new StoichiometryViolationException("Stoichiometry errors were found. Stopping game");
            }
        }

        private Aggregation aggregateInputBounds(List<GTRecipeInput> itemInputs,
                                                 List<GTRecipeInput> fluidInputs) {
            Map<Element, Bounds> totals = new HashMap<>();
            Map<Material, Fraction> unknowns = new HashMap<>();
            boolean hasStoichiometric = false;
            boolean hasAnyStacks = false;
            for (GTRecipeInput input : itemInputs) {
                Result result = decomposeItemInput(input);
                if (!result.bounds.isEmpty()) {
                    hasStoichiometric = true;
                }
                if (result.hasStacks) {
                    hasAnyStacks = true;
                }
                mergeBounds(totals, result.bounds);
                mergeUnknowns(unknowns, result.unknowns);
            }
            for (GTRecipeInput input : fluidInputs) {
                Result result = decomposeFluidInput(input);
                if (!result.bounds.isEmpty()) {
                    hasStoichiometric = true;
                }
                if (result.hasStacks) {
                    hasAnyStacks = true;
                }
                mergeBounds(totals, result.bounds);
                mergeUnknowns(unknowns, result.unknowns);
            }
            return new Aggregation(totals, unknowns, hasStoichiometric, hasAnyStacks);
        }

        private Aggregation aggregateOutputBounds(List<ItemStack> itemOutputs,
                                                  List<FluidStack> fluidOutputs) {
            Map<Element, Bounds> totals = new HashMap<>();
            Map<Material, Fraction> unknowns = new HashMap<>();
            boolean hasStoichiometric = false;
            boolean hasAnyStacks = false;
            
            // Process regular item outputs
            for (ItemStack stack : itemOutputs) {
                if (stack.isEmpty()) {
                    continue;
                }
                hasAnyStacks = true;
                Result contribution = decomposeItemStack(stack);
                if (!contribution.bounds.isEmpty()) {
                    hasStoichiometric = true;
                }
                mergeBounds(totals, contribution.bounds);
                mergeUnknowns(unknowns, contribution.unknowns);
            }
            
            // Process chanced item outputs
            for (ChancedItemOutput chancedOutput : recipe.getChancedOutputs().getChancedEntries()) {
                ItemStack stack = chancedOutput.getIngredient();
                if (stack.isEmpty()) {
                    continue;
                }
                hasAnyStacks = true;
                Result contribution = decomposeItemStack(stack);
                if (!contribution.bounds.isEmpty()) {
                    hasStoichiometric = true;
                }
                // Scale the bounds by the chance (chance is out of 10000)
                Fraction chance = Fraction.getFraction(chancedOutput.getChance(), CHANCE_DENOMINATOR);
                Map<Element, Bounds> scaledBounds = new HashMap<>();
                for (Map.Entry<Element, Bounds> entry : contribution.bounds.entrySet()) {
                    scaledBounds.put(entry.getKey(), entry.getValue().scale(chance));
                }
                mergeBounds(totals, scaledBounds);
                // Scale the unknown materials by the chance
                if (!contribution.unknowns.isEmpty()) {
                    Map<Material, Fraction> scaledUnknowns = new HashMap<>();
                    for (Map.Entry<Material, Fraction> entry : contribution.unknowns.entrySet()) {
                        scaledUnknowns.put(entry.getKey(), entry.getValue().multiplyBy(chance));
                    }
                    mergeUnknowns(unknowns, scaledUnknowns);
                } else {
                    mergeUnknowns(unknowns, contribution.unknowns);
                }
                if (chancedOutput.getChanceBoost() > 0) {
                    SussyPatches.LOGGER.warn("A chanced boost was detected in a stoichiometric recipe!");
                }
                mergeUnknowns(unknowns, contribution.unknowns);
            }
            
            // Process regular fluid outputs
            for (FluidStack stack : fluidOutputs) {
                if (stack != null && stack.amount > 0) {
                    hasAnyStacks = true;
                    Result contribution = decomposeFluidStack(stack);
                    if (!contribution.bounds.isEmpty()) {
                        hasStoichiometric = true;
                    }
                    mergeBounds(totals, contribution.bounds);
                    mergeUnknowns(unknowns, contribution.unknowns);
                }
            }
            
            // Process chanced fluid outputs
            for (ChancedFluidOutput chancedOutput : recipe.getChancedFluidOutputs().getChancedEntries()) {
                FluidStack stack = chancedOutput.getIngredient();
                if (stack != null && stack.amount > 0) {
                    hasAnyStacks = true;
                    Result contribution = decomposeFluidStack(stack);
                    if (!contribution.bounds.isEmpty()) {
                        hasStoichiometric = true;
                        // Scale the bounds by the chance (chance is out of 10000)
                        Fraction chance = Fraction.getFraction(chancedOutput.getChance(), CHANCE_DENOMINATOR);
                        Map<Element, Bounds> scaledBounds = new HashMap<>();
                        for (Map.Entry<Element, Bounds> entry : contribution.bounds.entrySet()) {
                            scaledBounds.put(entry.getKey(), entry.getValue().scale(chance));
                        }
                        mergeBounds(totals, scaledBounds);
                    }
                    // Scale the unknown materials by the chance
                    if (!contribution.unknowns.isEmpty()) {
                        Fraction chance = Fraction.getFraction(chancedOutput.getChance(), CHANCE_DENOMINATOR);
                        Map<Material, Fraction> scaledUnknowns = new HashMap<>();
                        for (Map.Entry<Material, Fraction> entry : contribution.unknowns.entrySet()) {
                            scaledUnknowns.put(entry.getKey(), entry.getValue().multiplyBy(chance));
                        }
                        mergeUnknowns(unknowns, scaledUnknowns);
                    } else {
                        mergeUnknowns(unknowns, contribution.unknowns);
                    }
                }
            }
            
            return new Aggregation(totals, unknowns, hasStoichiometric, hasAnyStacks);
        }

        private Result decomposeItemInput(GTRecipeInput input) {
            if (input.isNonConsumable() || !(input instanceof GTRecipeItemInput itemInput)) {
                return Result.empty();
            }
            ItemStack[] alternatives = itemInput.getInputStacks();
            if (alternatives.length == 0) {
                return Result.empty();
            }
            List<Result> altResults = new ArrayList<>(alternatives.length);
            for (ItemStack stack : alternatives) {
                if (!stack.isEmpty()) {
                    altResults.add(decomposeItemStack(stack));
                }
            }
            return combineAlternatives(altResults);
        }

        private Result decomposeFluidInput(GTRecipeInput input) {
            if (input.isNonConsumable() || !(input instanceof GTRecipeFluidInput fluidInput)) {
                return Result.empty();
            }
            FluidStack stack = fluidInput.getInputFluidStack();
            if (stack == null || stack.amount <= 0) {
                return Result.empty();
            }
            return decomposeFluidStack(stack);
        }

        private Result decomposeItemStack(ItemStack stack) {
            if (stack.isEmpty()) {
                return Result.empty();
            }
            Map<Element, Bounds> bounds = new HashMap<>();
            Map<Material, Fraction> unknowns = new HashMap<>();
            MaterialStack unmultipliedMaterialStack = getMaterial(stack);
            if (unmultipliedMaterialStack != null) {
                MaterialStack materialStack =
                        new MaterialStack(unmultipliedMaterialStack.material, unmultipliedMaterialStack.amount * stack.getCount());
                return decomposeOrePrefixItem(materialStack, bounds, unknowns);
            }
            ItemMaterialInfo info = getMaterialInfo(stack);
            if (info != null) {
                return decomposeItemMaterialInfo(info, unknowns, bounds);
            }
            return Result.of(bounds, unknowns, true);
        }

        private @NotNull Result decomposeItemMaterialInfo(ItemMaterialInfo info, Map<Material, Fraction> unknowns, Map<Element, Bounds> bounds) {
            for (MaterialStack component : info.getMaterials()) {
                Material material = component.material;
                if (material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                    continue;
                }
                Map<Element, Bounds> decomposition = decomposeMaterial(material,
                        materialAmount(component));
                if (decomposition.isEmpty()) {
                    unknowns.merge(material, materialAmount(component), Fraction::add);
                    continue;
                }
                mergeBounds(bounds, decomposition);
            }
            return Result.of(bounds, unknowns, true);
        }

        private @NotNull Result decomposeOrePrefixItem(MaterialStack materialStack, Map<Element, Bounds> bounds, Map<Material, Fraction> unknowns) {
            Material material = materialStack.material;

            if (material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                return Result.of(bounds, unknowns, true);
            }

            Map<Element, Bounds> decomposition = decomposeMaterial(material,
                    StoichiometryUtil.getMolesFromItem((int) materialStack.amount, material));
            if (decomposition.isEmpty()) {
                unknowns.put(material, Fraction.getFraction((int) materialStack.amount, 1));
                return Result.of(bounds, unknowns, true);
            }
            mergeBounds(bounds, decomposition);
            return Result.of(bounds, unknowns, true);
        }

        private Result decomposeFluidStack(FluidStack stack) {
            Material material = StoichiometryUtil.getMaterialFromFluid(stack);
            if (material == null || material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                Result result = Result.empty();
                result.markHasStacks();
                return result;
            }
            Map<Element, Bounds> bounds = decomposeMaterial(material, StoichiometryUtil.getMolesFromFluid(stack.amount, material));
            return Result.of(bounds, Map.of(), true);
        }

        private Map<Element, Bounds> decomposeMaterial(Material material, Fraction amount) {
            if (material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                return Map.of();
            }
            Map<Element, Bounds> resolved = graph.resolve(material, amount);
            if (!resolved.isEmpty()) {
                return resolved;
            }
            Map<Element, Bounds> inferred = INFERRED_BOUNDS.get(material);
            if (inferred != null) {
                Map<Element, Bounds> scaled = new HashMap<>();
                inferred.forEach((element, bounds) -> scaled.put(element, bounds.scale(amount)));
                return scaled;
            }
            return Map.of();
        }

        private Result combineAlternatives(List<Result> alternatives) {
            if (alternatives.isEmpty()) {
                return Result.empty();
            }
            Set<Element> elements = new HashSet<>();
            boolean hasStacks = false;
            Map<Material, Fraction> unknowns = new HashMap<>();
            for (Result result : alternatives) {
                elements.addAll(result.bounds.keySet());
                mergeUnknowns(unknowns, result.unknowns);
                hasStacks |= result.hasStacks;
            }
            Map<Element, Bounds> combined = new HashMap<>();
            for (Element element : elements) {
                Fraction min = null;
                Fraction max = null;
                for (Result result : alternatives) {
                    Bounds current = result.bounds.getOrDefault(element, Bounds.zero());
                    if (min == null || current.min.compareTo(min) < 0) {
                        min = current.min;
                    }
                    if (max == null || current.max.compareTo(max) > 0) {
                        max = current.max;
                    }
                }
                if (min == null) {
                    min = Fraction.ZERO;
                }
                if (max == null) {
                    max = Fraction.ZERO;
                }
                combined.put(element, new Bounds(min, max));
            }
            return Result.of(combined, unknowns, hasStacks);
        }



        private List<ElementViolation> diff(Aggregation inputs, Aggregation outputs) {
            Map<Element, Bounds> inputBounds = inputs.bounds();
            Map<Element, Bounds> outputBounds = outputs.bounds();
            Set<Element> elements = new HashSet<>(inputBounds.keySet());
            elements.addAll(outputBounds.keySet());
            List<ElementViolation> violations = new ArrayList<>();
            for (Element element : elements) {
                Bounds in = inputBounds.getOrDefault(element, Bounds.zero());
                Bounds out = outputBounds.getOrDefault(element, Bounds.zero());
                if (out.max.compareTo(in.min) < 0) {
                    boolean allowSink = !outputs.hasStoichiometric() && outputs.hasAnyStacks();
                    if (!lossy && !allowSink) {
                        violations.add(new ElementViolation(element, in, out));
                    }
                } else if (out.min.compareTo(in.max) > 0) {
                    violations.add(new ElementViolation(element, in, out));
                }
            }
            return violations;
        }

        private void processUnknowns(Aggregation inputs, Aggregation outputs) {
            Set<Material> materials = new HashSet<>(inputs.unknowns().keySet());
            materials.addAll(outputs.unknowns().keySet());
            if (materials.size() != 1) {
                // We need the StoichiometryState!

                return;
            }
            Material material = materials.iterator().next();
            Fraction inAmount = inputs.unknowns().getOrDefault(material, Fraction.ZERO);
            Fraction outAmount = outputs.unknowns().getOrDefault(material, Fraction.ZERO);
            Fraction net = outAmount.subtract(inAmount);
            if (net.equals(Fraction.ZERO)) {
                return;
            }
            if (lossy) {
                return;
            }

            boolean resolved = false;
            Map<Element, Bounds> perUnit;
            if (net.compareTo(Fraction.ZERO) > 0 && inAmount.equals(Fraction.ZERO)) {
                perUnit = inferPerUnit(inputs.bounds(), outputs.bounds(), net, material);
                applyInference(material, perUnit, net, outputs);
                outputs.unknowns().remove(material);
                resolved = true;
            } else if (net.compareTo(Fraction.ZERO) < 0 && outAmount.equals(Fraction.ZERO)) {
                Fraction consumed = net.negate();
                perUnit = inferPerUnit(outputs.bounds(), inputs.bounds(), consumed, material);
                applyInference(material, perUnit, consumed, inputs);
                inputs.unknowns().remove(material);
                resolved = true;
            }

            if (!resolved) {
                throw new StoichiometryViolationException("Cannot resolve stoichiometry for material " + material.getLocalizedName());
            }
        }

        private Map<Element, Bounds> inferPerUnit(Map<Element, Bounds> primary, Map<Element, Bounds> secondary,
                                                  Fraction amount, Material material) {
            if (amount.equals(Fraction.ZERO)) {
                return Map.of();
            }
            Map<Element, Bounds> perUnit = new HashMap<>();
            Fraction reciprocal = reciprocal(amount);
            Set<Element> elements = new HashSet<>(primary.keySet());
            elements.addAll(secondary.keySet());
            Map<Element, Bounds> existing = INFERRED_BOUNDS.get(material);
            if (existing != null) {
                elements.addAll(existing.keySet());
            }
            for (Element element : elements) {
                Bounds primaryBounds = primary.getOrDefault(element, Bounds.zero());
                Bounds secondaryBounds = secondary.getOrDefault(element, Bounds.zero());
                Bounds rawDelta = Bounds.subtract(primaryBounds, secondaryBounds);
                if (rawDelta.max.compareTo(Fraction.ZERO) < 0) {
                    throw new StoichiometryViolationException("Unknown material inference resulted in negative contribution for " + element.getSymbol());
                }
                Fraction min = rawDelta.min.compareTo(Fraction.ZERO) < 0 ? Fraction.ZERO : rawDelta.min;
                Bounds delta = new Bounds(min, rawDelta.max);
                Bounds perUnitBounds = delta.scale(reciprocal);
                Bounds stored = existing != null ? existing.get(element) : null;
                if (stored != null) {
                    Bounds intersection = stored.intersect(perUnitBounds);
                    if (intersection == null) {
                        throw new StoichiometryViolationException("Inconsistent stoichiometry for material " + material.getLocalizedName());
                    }
                    perUnitBounds = intersection;
                }
                perUnit.put(element, perUnitBounds);
            }
            return perUnit;
        }

        private void applyInference(Material material, Map<Element, Bounds> perUnit, Fraction amount,
                                    Aggregation target) {
            if (perUnit.isEmpty()) {
                return;
            }
            storeInferred(material, perUnit);
            Map<Element, Bounds> scaled = new HashMap<>();
            perUnit.forEach((element, bounds) -> scaled.put(element, bounds.scale(amount)));
            mergeBounds(target.bounds(), scaled);
            target.setStoichiometric();
        }

        private void storeInferred(Material material, Map<Element, Bounds> perUnit) {
            Map<Element, Bounds> stored = INFERRED_BOUNDS.computeIfAbsent(material, m -> new HashMap<>());
            perUnit.forEach((element, bounds) -> {
                Bounds current = stored.get(element);
                if (current == null) {
                    stored.put(element, bounds);
                } else {
                    Bounds intersection = current.intersect(bounds);
                    if (intersection == null) {
                        throw new StoichiometryViolationException("Inconsistent stoichiometry for material " + material.getLocalizedName());
                    }
                    stored.put(element, intersection);
                }
            });
        }

        private static final class Aggregation {

            private final Map<Element, Bounds> bounds;
            private final Map<Material, Fraction> unknowns;
            private boolean hasStoichiometric;
            private boolean hasAnyStacks;

            private Aggregation(Map<Element, Bounds> bounds, Map<Material, Fraction> unknowns,
                                boolean hasStoichiometric, boolean hasAnyStacks) {
                this.bounds = bounds;
                this.unknowns = unknowns;
                this.hasStoichiometric = hasStoichiometric;
                this.hasAnyStacks = hasAnyStacks;
            }

            Map<Element, Bounds> bounds() {
                return bounds;
            }

            Map<Material, Fraction> unknowns() {
                return unknowns;
            }

            boolean hasStoichiometric() {
                return hasStoichiometric;
            }

            void setStoichiometric() {
                this.hasStoichiometric = true;
            }

            boolean hasAnyStacks() {
                return hasAnyStacks;
            }

            void markHasStacks() {
                this.hasAnyStacks = true;
            }
        }

        private static final class Result {

            private final Map<Element, Bounds> bounds;
            private final Map<Material, Fraction> unknowns;
            private boolean hasStacks;

            private Result(Map<Element, Bounds> bounds, Map<Material, Fraction> unknowns, boolean hasStacks) {
                this.bounds = bounds;
                this.unknowns = unknowns;
                this.hasStacks = hasStacks;
            }

            static Result empty() {
                return new Result(new HashMap<>(), new HashMap<>(), false);
            }

            static Result of(Map<Element, Bounds> bounds, Map<Material, Fraction> unknowns, boolean hasStacks) {
                return new Result(new HashMap<>(bounds), new HashMap<>(unknowns), hasStacks);
            }

            Map<Element, Bounds> bounds() {
                return bounds;
            }

            Map<Material, Fraction> unknowns() {
                return unknowns;
            }

            boolean hasBounds() {
                return !bounds.isEmpty();
            }

            boolean hasStacks() {
                return hasStacks;
            }

            void markHasStacks() {
                this.hasStacks = true;
            }

            void merge(Result other) {
                mergeBounds(this.bounds, other.bounds);
                mergeUnknowns(this.unknowns, other.unknowns);
                if (other.hasStacks) {
                    this.hasStacks = true;
                }
            }
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

    private record Bounds(Fraction min, Fraction max) {

        static Bounds zero() {
            return new Bounds(Fraction.ZERO, Fraction.ZERO);
        }

        static Bounds exact(long value) {
            Fraction fraction = Fraction.getFraction((int) value, 1);
            return new Bounds(fraction, fraction);
        }

        static Bounds exact(Fraction value) {
            return new Bounds(value, value);
        }

        static Bounds combine(Bounds a, Bounds b) {
            return new Bounds(a.min.add(b.min), a.max.add(b.max));
        }

        static Bounds subtract(Bounds left, Bounds right) {
            return new Bounds(left.min.subtract(right.max), left.max.subtract(right.min));
        }

        Bounds scale(long factor) {
            return scale(Fraction.getFraction((int) factor, 1));
        }

        Bounds scale(Fraction factor) {
            if (factor.equals(Fraction.ONE)) {
                return this;
            }
            return new Bounds(min.multiplyBy(factor), max.multiplyBy(factor));
        }

        Bounds intersect(Bounds other) {
            Fraction newMin = greaterOf(this.min, other.min);
            Fraction newMax = lesserOf(this.max, other.max);
            if (newMin.compareTo(newMax) > 0) {
                return null;
            }
            return new Bounds(newMin, newMax);
        }

        private static Fraction greaterOf(Fraction a, Fraction b) {
            return a.compareTo(b) >= 0 ? a : b;
        }

        private static Fraction lesserOf(Fraction a, Fraction b) {
            return a.compareTo(b) <= 0 ? a : b;
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

        Map<Element, Bounds> resolve(Material material, Fraction amount) {
            if (amount.compareTo(Fraction.ZERO) <= 0) {
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
                Fraction multiplier = materialAmount(component);
                childBounds.forEach((element, bounds) ->
                        scaled.put(element, bounds.scale(multiplier)));
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
                return Map.of(element, Bounds.exact(1));
            }
            return new HashMap<>();
        }
    }

    private static Collection<Material> getRegisteredMaterials() {
        IMaterialRegistryManager manager = MaterialRegistryManager.getInstance();
        return manager.getRegisteredMaterials();
    }

    public static class StoichiometryViolationException extends RuntimeException {
        StoichiometryViolationException(String message) {
            super(message);
        }
    }
}
