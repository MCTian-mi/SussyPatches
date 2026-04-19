package dev.tianmi.sussypatches.common.stoichiometry;

import static gregtech.api.unification.OreDictUnifier.getMaterial;
import static gregtech.api.unification.OreDictUnifier.getMaterialInfo;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import dev.tianmi.sussypatches.SussyPatches;
import dev.tianmi.sussypatches.api.recipe.property.StoichiometryProperty;
import dev.tianmi.sussypatches.api.unification.SusMaterialFlags;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.core.unification.material.internal.MaterialRegistryManager;

/**
 * Debug-only stoichiometry verifier to ensure scripted recipes conserve elements.
 */
public final class StoichiometryVerifier {

    public static final int CHANCE_DENOMINATOR = 10000;
    public static final StoichiometryState stoichiometryState = new StoichiometryState();
    private static final MaterialGraph graph = MaterialGraph.build();

    private static final Map<Material, Map<Material, Fraction>> INFERRED_BOUNDS = new HashMap<>();

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
        run(recipe, settings.lossy());
    }

    private static boolean shouldVerify(RecipeMap<?> map, Recipe recipe) {
        if (!SusConfig.DEBUG.enableStoichiometryVerifier) return false;
        if (!recipe.isGroovyRecipe()) return false;
        String name = map.unlocalizedName;
        for (String allowed : SusConfig.DEBUG.stoichiometryRecipeMaps) {
            if (allowed.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private static void mergeCompositions(Map<Material, Fraction> totals, Map<Material, Fraction> addition) {
        addition.forEach((material, amount) -> totals.merge(material, amount, Fraction::add));
    }

    private static Fraction materialAmount(MaterialStack stack) {
        return Fraction.getFraction((int) stack.amount);
    }

    private static void run(Recipe recipe, boolean lossy) {
        Aggregation input = aggregateInputBounds(recipe.getInputs(), recipe.getFluidInputs());
        Aggregation output = aggregateOutputBounds(recipe.getOutputs(), recipe.getFluidOutputs(),
                recipe.getChancedOutputs(), recipe.getChancedFluidOutputs());

        if (input.unboundedAbove) {
            return;
        }
        lossy |= output.unboundedAbove;
        try {
            boolean hasInputUnknowns = hasUnknowns(input.composition);
            boolean hasOutputUnknowns = hasUnknowns(output.composition);
            if (!hasInputUnknowns) {
                List<ElementViolation> violations = diff(input, output, lossy | hasOutputUnknowns);
                if (!violations.isEmpty()) {
                    String message = format(violations);
                    throw new StoichiometryViolationException(message);
                }
            }
            if (hasInputUnknowns || hasOutputUnknowns) {
                stoichiometryState.addReaction(input.composition, output.composition, lossy);
            }
        } catch (StoichiometryViolationException e) {
            if (SusConfig.DEBUG.stoichiometryThrowOnViolation) {
                throw e;
            } else {
                SussyPatches.LOGGER.error(e);
                StackTraceElement[] elements = e.getStackTrace();
                for (int i = 0; i < Math.min(elements.length, 20); i++) {
                    if (elements[i].getMethodName().equals("buildAndRegister")) {
                        SussyPatches.LOGGER.error("at {}\n{}\n{}\n{}", elements[i + 1], elements[i + 2],
                                elements[i + 3], elements[i + 4]);
                        return;
                    }
                }
            }
        }
    }

    private static boolean hasUnknowns(Map<Material, Fraction> set) {
        for (Material m : set.keySet()) {
            if (!m.isElement()) {
                return true;
            }
        }
        return false;
    }

    private static Aggregation aggregateInputBounds(List<GTRecipeInput> itemInputs,
                                                    List<GTRecipeInput> fluidInputs) {
        Map<Material, Fraction> composition = new HashMap<>();
        boolean unboundedAbove = false;
        for (GTRecipeInput input : itemInputs) {
            Result result = decomposeItemInput(input);
            if (result.unboundedAbove) {
                unboundedAbove = true;
            }
            mergeCompositions(composition, result.composition);
        }
        for (GTRecipeInput input : fluidInputs) {
            Result result = decomposeFluidInput(input);
            if (result.unboundedAbove) {
                unboundedAbove = true;
            }
            mergeCompositions(composition, result.composition);
        }
        return new Aggregation(composition, unboundedAbove);
    }

    private static Aggregation aggregateOutputBounds(List<ItemStack> itemOutputs,
                                                     List<FluidStack> fluidOutputs,
                                                     ChancedOutputList<ItemStack, ChancedItemOutput> chancedItemOutputs,
                                                     ChancedOutputList<FluidStack, ChancedFluidOutput> chancedFluidOutputs) {
        Map<Material, Fraction> composition = new HashMap<>();
        boolean unboundedAbove = false;

        // Process regular item outputs
        for (ItemStack stack : itemOutputs) {
            if (stack.isEmpty()) {
                continue;
            }
            Result contribution = decomposeItemStack(stack);
            if (contribution.unboundedAbove) {
                unboundedAbove = true;
            }
            mergeCompositions(composition, contribution.composition);
        }

        // Process chanced item outputs
        for (ChancedItemOutput chancedOutput : chancedItemOutputs.getChancedEntries()) {
            ItemStack stack = chancedOutput.getIngredient();
            if (stack.isEmpty()) {
                continue;
            }
            Result contribution = decomposeItemStack(stack);
            if (contribution.unboundedAbove) {
                unboundedAbove = true;
            }
            // Scale the bounds by the chance (chance is out of 10000)
            Fraction chance = Fraction.getFraction(chancedOutput.getChance(), CHANCE_DENOMINATOR);
            Map<Material, Fraction> scaledComp = new HashMap<>();
            for (Map.Entry<Material, Fraction> entry : contribution.composition.entrySet()) {
                scaledComp.put(entry.getKey(), entry.getValue().multiplyBy(chance));
            }
            mergeCompositions(composition, scaledComp);

            if (chancedOutput.getChanceBoost() > 0) {
                SussyPatches.LOGGER.warn("A chanced boost was detected in a stoichiometric recipe!");
            }
        }

        // Process regular fluid outputs
        for (FluidStack stack : fluidOutputs) {
            if (stack != null && stack.amount > 0) {
                Result contribution = decomposeFluidStack(stack);
                if (contribution.unboundedAbove) {
                    unboundedAbove = true;
                }
                mergeCompositions(composition, contribution.composition);
            }
        }

        // Process chanced fluid outputs
        for (ChancedFluidOutput chancedOutput : chancedFluidOutputs.getChancedEntries()) {
            FluidStack stack = chancedOutput.getIngredient();
            if (stack != null && stack.amount > 0) {
                Result contribution = decomposeFluidStack(stack);
                if (contribution.unboundedAbove) {
                    unboundedAbove = true;
                }
                // Scale the bounds by the chance (chance is out of 10000)
                Fraction chance = Fraction.getFraction(chancedOutput.getChance(), CHANCE_DENOMINATOR);
                Map<Material, Fraction> scaledComp = new HashMap<>();
                for (Map.Entry<Material, Fraction> entry : contribution.composition.entrySet()) {
                    scaledComp.put(entry.getKey(), entry.getValue().multiplyBy(chance));
                }
                mergeCompositions(composition, scaledComp);

                if (chancedOutput.getChanceBoost() > 0) {
                    SussyPatches.LOGGER.warn("A chanced boost was detected in a stoichiometric recipe!");
                }
            }
        }

        return new Aggregation(composition, unboundedAbove);
    }

    private static Result decomposeItemInput(GTRecipeInput input) {
        if (input.isNonConsumable()) {
            return Result.empty();
        }
        ItemStack[] alternatives = input.getInputStacks();
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

    private static Result decomposeFluidInput(GTRecipeInput input) {
        if (input.isNonConsumable() || !(input instanceof GTRecipeFluidInput fluidInput)) {
            return Result.empty();
        }
        FluidStack stack = fluidInput.getInputFluidStack();
        if (stack == null || stack.amount <= 0) {
            return Result.empty();
        }
        return decomposeFluidStack(stack);
    }

    private static Result decomposeItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return Result.empty();
        }
        Map<Material, Fraction> composition = new HashMap<>();
        MaterialStack unmultipliedMaterialStack = getMaterial(stack);
        if (unmultipliedMaterialStack != null) {
            Material mat = unmultipliedMaterialStack.material;
            // Catches things like marker materials
            if (mat.isElement() || mat.getMaterialComponents() != null) {
                MaterialStack materialStack = new MaterialStack(unmultipliedMaterialStack.material,
                        unmultipliedMaterialStack.amount * stack.getCount());
                return decomposeOrePrefixItem(materialStack, composition);
            }
        }
        ItemMaterialInfo info = getMaterialInfo(stack);
        if (info != null) {
            return decomposeItemMaterialInfo(info, composition);
        }
        Material mat = ItemDummyMaterial.get(stack);
        composition.put(mat, Fraction.getFraction(stack.getCount()));
        return Result.of(composition, false);
    }

    private static @NotNull Result decomposeItemMaterialInfo(ItemMaterialInfo info,
                                                             Map<Material, Fraction> composition) {
        boolean unboundedAbove = false;
        for (MaterialStack component : info.getMaterials()) {
            Material material = component.material;
            if (material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                unboundedAbove = true;
            }
            Map<Material, Fraction> decomposition = decomposeMaterial(material,
                    materialAmount(component));
            mergeCompositions(composition, decomposition);
        }
        return Result.of(composition, unboundedAbove);
    }

    private static @NotNull Result decomposeOrePrefixItem(MaterialStack materialStack,
                                                          Map<Material, Fraction> composition) {
        Material material = materialStack.material;

        if (material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
            return Result.of(composition, true);
        }

        Map<Material, Fraction> decomposition = decomposeMaterial(material,
                StoichiometryUtil.getMolesFromItem(Fraction.getFraction((int) materialStack.amount, (int) GTValues.M),
                        material));

        mergeCompositions(composition, decomposition);
        return Result.of(composition, false);
    }

    private static Result decomposeFluidStack(FluidStack stack) {
        boolean unboundedAbove = false;
        Material material = StoichiometryUtil.getMaterialFromFluid(stack);
        if (material == null || material.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
            unboundedAbove = true;
        }
        Map<Material, Fraction> composition = decomposeMaterial(material,
                StoichiometryUtil.getMolesFromFluid(stack.amount, material));
        return Result.of(composition, unboundedAbove);
    }

    private static Map<Material, Fraction> decomposeMaterial(Material material, Fraction amount) {
        Map<Material, Fraction> resolved = graph.resolve(material, amount);
        if (!resolved.isEmpty()) {
            return resolved;
        }
        Map<Material, Fraction> inferred = INFERRED_BOUNDS.get(material);
        if (inferred != null) {
            Map<Material, Fraction> scaled = new HashMap<>();
            inferred.forEach((element, fraction) -> scaled.put(element, fraction.multiplyBy(amount)));
            return scaled;
        }
        return Map.of();
    }

    private static Result combineAlternatives(List<Result> alternatives) {
        if (alternatives.isEmpty()) {
            return Result.empty();
        }
        boolean hasStacks = false;
        Map<Material, Fraction> composition = new HashMap<>();
        // Take the minimum of everything for the worst possible input case
        alternatives.forEach((result) -> result.composition.forEach(
                (material, amount) -> composition.merge(material, amount, (x, y) -> x.compareTo(y) < 0 ? x : y)));
        return Result.of(composition, hasStacks);
    }

    private static List<ElementViolation> diff(Aggregation inputs, Aggregation outputs, boolean lossy) {
        Map<Material, Fraction> inputComp = inputs.composition;
        Map<Material, Fraction> outputComp = outputs.composition;
        Set<Material> mats = new HashSet<>(inputComp.keySet());
        mats.addAll(outputComp.keySet());
        mats.removeIf(mat -> !mat.isElement());
        List<ElementViolation> violations = new ArrayList<>();
        for (Material element : mats) {
            Fraction in = inputComp.getOrDefault(element, Fraction.ZERO);
            Fraction out = outputComp.getOrDefault(element, Fraction.ZERO);
            if (out.compareTo(in) < 0) {
                if (!lossy) {
                    violations.add(new ElementViolation(element.getElement(), in, out));
                }
            } else if (out.compareTo(in) > 0) {
                violations.add(new ElementViolation(element.getElement(), in, out));
            }
        }
        return violations;
    }

    private static final class Aggregation {

        private final Map<Material, Fraction> composition;
        private boolean unboundedAbove;

        private Aggregation(Map<Material, Fraction> composition,
                            boolean unboundedAbove) {
            this.composition = composition;
            this.unboundedAbove = unboundedAbove;
        }
    }

    private static final class Result {

        private final Map<Material, Fraction> composition;
        private boolean unboundedAbove;

        private Result(Map<Material, Fraction> composition, boolean unboundedAbove) {
            this.composition = composition;
            this.unboundedAbove = unboundedAbove;
        }

        static Result empty() {
            return new Result(new HashMap<>(), false);
        }

        static Result of(Map<Material, Fraction> composition, boolean unboundedAbove) {
            return new Result(new HashMap<>(composition), unboundedAbove);
        }
    }

    private static String format(List<ElementViolation> violations) {
        String elementDetails = violations.stream()
                .map(v -> String.format(Locale.ROOT, "  - %s: inputs %s -> outputs %s",
                        v.element.getSymbol(), v.input, v.output))
                .collect(Collectors.joining("\n"));

        return "Stoichiometry violation detected: \n" +
                elementDetails;
    }

    private record ElementViolation(Element element, Fraction input, Fraction output) {}

    private static final class MaterialGraph {

        private final Map<Material, Node> nodes;
        private final Map<Material, Map<Material, Fraction>> cache = new HashMap<>();

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

        Map<Material, Fraction> resolve(Material material, Fraction amount) {
            if (amount.compareTo(Fraction.ZERO) <= 0) {
                return Map.of();
            }
            Map<Material, Fraction> perUnit = resolvePerUnit(material, new HashSet<>());
            if (perUnit.isEmpty()) {
                return perUnit;
            }
            Map<Material, Fraction> scaled = new HashMap<>();
            perUnit.forEach((element, quantity) -> scaled.put(element, quantity.multiplyBy(amount)));
            return scaled;
        }

        private Map<Material, Fraction> resolvePerUnit(Material material, Set<Material> visiting) {
            Map<Material, Fraction> cached = cache.get(material);
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

            Map<Material, Fraction> totals = new HashMap<>(node.getDirectElementBounds());
            for (MaterialStack component : node.getComponents()) {
                Material child = component.material;
                if (child.hasFlag(SusMaterialFlags.NON_STOICHIOMETRIC)) {
                    continue;
                }
                Map<Material, Fraction> childBounds = resolvePerUnit(child, visiting);
                if (childBounds.isEmpty()) {
                    continue;
                }
                Map<Material, Fraction> scaled = new HashMap<>();
                Fraction multiplier = materialAmount(component);
                childBounds.forEach((element, fraction) -> scaled.put(element, fraction.multiplyBy(multiplier)));
                mergeCompositions(totals, scaled);
            }

            visiting.remove(material);
            cache.put(material, totals);
            return totals;
        }
    }

    private static final class Node {

        private final Map<Material, Fraction> directElementBounds;
        private final ImmutableList<MaterialStack> components;

        Node(Material material) {
            this.directElementBounds = computeDirect(material);
            this.components = material.getMaterialComponents();
        }

        Map<Material, Fraction> getDirectElementBounds() {
            return directElementBounds;
        }

        ImmutableList<MaterialStack> getComponents() {
            return components;
        }

        private Map<Material, Fraction> computeDirect(Material material) {
            ImmutableList<MaterialStack> components = material.getMaterialComponents();
            if (components.isEmpty()) {
                return Map.of(material, Fraction.ONE);
            }
            return new HashMap<>();
        }
    }

    private static Collection<Material> getRegisteredMaterials() {
        IMaterialRegistryManager manager = MaterialRegistryManager.getInstance();
        return manager.getRegisteredMaterials();
    }
}
