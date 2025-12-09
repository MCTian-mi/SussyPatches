package dev.tianmi.sussypatches.common.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gregtech.api.GTValues;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.modules.ModuleManager;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.tianmi.sussypatches.api.recipe.property.StoichiometryProperty;
import dev.tianmi.sussypatches.api.unification.SusMaterialFlags;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.GregTechAPI;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.output.ChancedOutputList;
import gregtech.api.recipes.chance.output.ChancedOutputLogic;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.recipeproperties.RecipePropertyStorage;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class StoichiometryVerifierTest {
    private static final TestMaterialRegistryManager MATERIAL_MANAGER = new TestMaterialRegistryManager();

    static {
        GregTechAPI.materialManager = MATERIAL_MANAGER;
        net.minecraft.init.Bootstrap.register();
        ModMetadata meta = new ModMetadata();
        meta.modId = GTValues.MODID;
        Loader.instance().setupTestHarness(new DummyModContainer(meta));
        GregTechAPI.moduleManager = ModuleManager.getInstance();
    }

    private static final String TEST_MOD = "sussypatches_test";
    private static final String TEST_MAP_NAME = "test_map";


    private static final Material HYDROGEN = createElementMaterial(1, "hydrogen", Elements.H);
    private static final Material OXYGEN = createElementMaterial(2, "oxygen", Elements.O);
    private static final Material WATER = createCompositeMaterial(3, "water", HYDROGEN, 2, OXYGEN, 1);
    private static final Material WASTEWATER = createFlaggedMaterial(4, "wastewater", SusMaterialFlags.NON_STOICHIOMETRIC);

    private static final ItemStack HYDROGEN_INPUT = registerMaterialItem("hydrogen_input", HYDROGEN, 2);
    private static final ItemStack OXYGEN_INPUT = registerMaterialItem("oxygen_input", OXYGEN, 1);
    private static final ItemStack WATER_OUTPUT = registerMaterialItem("water_output", WATER, 1);
    private static final ItemStack HYDROGEN_OUTPUT = registerMaterialItem("hydrogen_output", HYDROGEN, 2);
    private static final ItemStack WASTEWATER_OUTPUT = registerMaterialItem("wastewater_output", WASTEWATER, 1);

    private static final RecipeMap<TestRecipeBuilder> TEST_MAP =
            new RecipeMap<>(TEST_MAP_NAME, 4, 4, 4, 4, new TestRecipeBuilder(), false);
    private static final gregtech.api.recipes.category.GTRecipeCategory TEST_CATEGORY =
            gregtech.api.recipes.category.GTRecipeCategory.create(TEST_MOD, "test_category",
                    TEST_MOD + ".test_category", TEST_MAP);

    @BeforeAll
    static void configureEnvironment() {
        SusConfig.DEBUG.enableStoichiometryVerifier = true;
    }

    @BeforeEach
    void resetConfig() {
        SusConfig.DEBUG.stoichiometryRecipeMaps = new String[] { TEST_MAP_NAME };
        SusConfig.DEBUG.stoichiometryThrowOnViolation = false;
    }

    @Test
    void balancedRecipePassesVerification() {
        Recipe recipe = createRecipe(Arrays.asList(
                new GTRecipeItemInput(HYDROGEN_INPUT.copy()),
                new GTRecipeItemInput(OXYGEN_INPUT.copy())),
                Collections.singletonList(WATER_OUTPUT.copy()),
                null);

        markGroovy(recipe);

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(recipe, TEST_MAP));
    }

    @Test
    void violationThrowsWhenConfigured() {
        Recipe recipe = createRecipe(Arrays.asList(
                new GTRecipeItemInput(HYDROGEN_INPUT.copy()),
                new GTRecipeItemInput(OXYGEN_INPUT.copy())),
                Collections.singletonList(HYDROGEN_OUTPUT.copy()),
                null);

        markGroovy(recipe);
        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertThrows(RuntimeException.class, () -> StoichiometryVerifier.verify(recipe, TEST_MAP));
    }

    @Test
    void lossyRecipesAllowMassLoss() {
        Recipe recipe = createRecipe(Arrays.asList(
                new GTRecipeItemInput(HYDROGEN_INPUT.copy()),
                new GTRecipeItemInput(OXYGEN_INPUT.copy())),
                Collections.singletonList(HYDROGEN_OUTPUT.copy()),
                StoichiometryProperty.lossy());

        markGroovy(recipe);
        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(recipe, TEST_MAP));
    }

    @Test
    void verificationDisabledViaProperty() {
        Recipe recipe = createRecipe(Arrays.asList(
                new GTRecipeItemInput(HYDROGEN_INPUT.copy()),
                new GTRecipeItemInput(OXYGEN_INPUT.copy())),
                Collections.singletonList(HYDROGEN_OUTPUT.copy()),
                StoichiometryProperty.disableVerifier());

        markGroovy(recipe);
        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(recipe, TEST_MAP));
    }

    @Test
    void nonStoichiometricMaterialsAreIgnored() {
        Recipe recipe = createRecipe(Arrays.asList(
                new GTRecipeItemInput(HYDROGEN_INPUT.copy()),
                new GTRecipeItemInput(OXYGEN_INPUT.copy())),
                Collections.singletonList(WASTEWATER_OUTPUT.copy()),
                null);

        markGroovy(recipe);
        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(recipe, TEST_MAP));
    }

    private static Recipe createRecipe(List<GTRecipeInput> itemInputs, List<ItemStack> outputs,
                                       StoichiometryProperty.Settings settings) {
        RecipePropertyStorage storage = null;
        if (settings != null) {
            storage = new RecipePropertyStorage();
            storage.store(StoichiometryProperty.getInstance(), settings);
        }

        return new Recipe(itemInputs,
                outputs,
                new ChancedOutputList<>(ChancedOutputLogic.NONE, Collections.emptyList()),
                Collections.emptyList(),
                Collections.emptyList(),
                new ChancedOutputList<>(ChancedOutputLogic.NONE, Collections.emptyList()),
                1,
                1,
                false,
                false,
                storage,
                TEST_CATEGORY);
    }

    private static void markGroovy(Recipe recipe) {
        try {
            Field field = Recipe.class.getDeclaredField("groovyRecipe");
            field.setAccessible(true);
            field.setBoolean(recipe, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Material createElementMaterial(int id, String path, Element element) {
        Material.Builder builder = new Material.Builder(id, new ResourceLocation(TEST_MOD, path));
        builder.element(element);
        return builder.build();
    }

    private static Material createCompositeMaterial(int id, String path, Object... components) {
        Material.Builder builder = new Material.Builder(id, new ResourceLocation(TEST_MOD, path));
        builder.components(components);
        return builder.build();
    }

    private static Material createFlaggedMaterial(int id, String path, MaterialFlag flag) {
        Material.Builder builder = new Material.Builder(id, new ResourceLocation(TEST_MOD, path));
        builder.flags(flag);
        return builder.build();
    }

    private static ItemStack registerMaterialItem(String name, Material material, long amount) {
        TestItem item = new TestItem(new ResourceLocation(TEST_MOD, name));
        ItemStack stack = new ItemStack(item);
        OreDictUnifier.registerOre(stack.copy(), new ItemMaterialInfo(new MaterialStack(material, amount)));
        return stack;
    }

    private static class TestRecipeBuilder extends RecipeBuilder<TestRecipeBuilder> {
        protected TestRecipeBuilder() {
            super();
        }
    }

    private static class TestItem extends Item {
        TestItem(ResourceLocation id) {
            setRegistryName(id);
        }
    }

    private static class TestMaterialRegistryManager implements IMaterialRegistryManager {

        private final Map<String, TestMaterialRegistry> registries = new HashMap<>();
        private Phase phase = Phase.OPEN;

        @Override
        public MaterialRegistry createRegistry(String modid) {
            TestMaterialRegistry registry = new TestMaterialRegistry(modid);
            registries.put(modid, registry);
            return registry;
        }

        @Override
        public MaterialRegistry getRegistry(String modid) {
            return registries.computeIfAbsent(modid, TestMaterialRegistry::new);
        }

        @Override
        public MaterialRegistry getRegistry(int networkId) {
            return getRegistry(TEST_MOD);
        }

        @Override
        public Collection<MaterialRegistry> getRegistries() {
            return new ArrayList<>(registries.values());
        }

        @Override
        public Collection<Material> getRegisteredMaterials() {
            List<Material> materials = new ArrayList<>();
            registries.values().forEach(registry -> materials.addAll(registry.getAllMaterials()));
            return materials;
        }

        @Override
        public Material getMaterial(String name) {
            for (MaterialRegistry registry : registries.values()) {
                Material material = registry.getObject(name);
                if (material != null) {
                    return material;
                }
            }
            return null;
        }

        @Override
        public Phase getPhase() {
            return phase;
        }
    }

    private static class TestMaterialRegistry extends MaterialRegistry {

        private final String modid;
        private final Map<String, Material> entries = new HashMap<>();
        private Material fallback;

        TestMaterialRegistry(String modid) {
            this.modid = modid;
        }

        @Override
        public void register(Material material) {
            entries.put(material.getName(), material);
            if (fallback == null) {
                fallback = material;
            }
        }

        @Override
        public Collection<Material> getAllMaterials() {
            return entries.values();
        }

        @Override
        public void setFallbackMaterial(Material material) {
            this.fallback = material;
        }

        @Override
        public Material getFallbackMaterial() {
            return fallback;
        }

        @Override
        public int getNetworkId() {
            return 0;
        }

        @Override
        public String getModid() {
            return modid;
        }
    }
}
