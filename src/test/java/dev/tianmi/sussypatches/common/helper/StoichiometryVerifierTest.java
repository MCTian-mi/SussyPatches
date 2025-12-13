package dev.tianmi.sussypatches.common.helper;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import dev.tianmi.sussypatches.api.unification.material.properties.MolarProperty;
import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluidRegistration;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.common.CommonProxy;
import gregtech.common.items.MetaItems;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import gregtech.modules.ModuleManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.ObjectHolderRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.junit.jupiter.api.Assertions;
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
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class StoichiometryVerifierTest {

    private static MaterialRegistryManager managerInternal;

    static {
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            deobfuscatedEnvironment.setBoolean(null, true);

            Method setLocale = I18n.class.getDeclaredMethod("setLocale", Locale.class); // No need to care about
            // obfuscation
            setLocale.setAccessible(true);
            setLocale.invoke(null, new Locale());

            // set FMLCommonHandler#sidedDelegate, since MaterialIconType and LocalizationUtils uses it
            Field sidedDelegate = FMLCommonHandler.class.getDeclaredField("sidedDelegate");
            sidedDelegate.setAccessible(true);
            sidedDelegate.set(FMLCommonHandler.instance(), new TestSidedHandler());
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Unexpected exception on test bootstrap", e);
        }
        managerInternal = MaterialRegistryManager.getInstance();
        GregTechAPI.materialManager = managerInternal;
        GregTechAPI.markerMaterialRegistry = MarkerMaterialRegistry.getInstance();

        net.minecraft.init.Bootstrap.register();
        ModMetadata meta = new ModMetadata();
        meta.modId = GTValues.MODID;
        Loader.instance().setupTestHarness(new DummyModContainer(meta));
        GregTechAPI.moduleManager = ModuleManager.getInstance();
        managerInternal.unfreezeRegistries();
        Materials.register();
    }

    private static final String TEST_MOD = "sussypatches_test";
    private static final String TEST_MAP_NAME = "test_map";


    private static final Material Wastewater;
    private static final Material Brubium;
    private static final Material Zalgonium;
    private static final Material DilutedBrubium;

    private static final RecipeMap<TestRecipeBuilder> TEST_MAP =
            new RecipeMap<>(TEST_MAP_NAME, 4, 4, 4, 4, new TestRecipeBuilder(), false);

    static {
        Wastewater = new Material.Builder(20000, new ResourceLocation(GTValues.MODID, "wastewater"))
                .fluid()
                .flags(SusMaterialFlags.NON_STOICHIOMETRIC)
                .build();
        Brubium = new Material.Builder(20001, new ResourceLocation(GTValues.MODID, "brubium"))
                .dust()
                .build();
        Zalgonium = new Material.Builder(20002, new ResourceLocation(GTValues.MODID, "zalgonium"))
                .dust()
                .fluid()
                .build();
        DilutedBrubium = new Material.Builder(20003, new ResourceLocation(GTValues.MODID, "diluted_brubium"))
                .fluid()
                .build();

        Ice.setProperty(MolarProperty.MOLAR, MolarProperty.fromFluidConversion(1000, 144));
        BandedIron.addFlags(SusMaterialFlags.SINGLE_ITEM_MOLE);

        managerInternal.closeRegistries();
        managerInternal.freezeRegistries();
        GTFluidRegistration.INSTANCE.register();
        OreDictUnifier.init();

        MetaItems.init();
        for (MetaItem<?> item : MetaItems.ITEMS) {
            if (item instanceof MetaPrefixItem) {
                item.registerSubItems();
                for (MetaItem.MetaValueItem i : item.getAllItems()) {
                    // The unlocalized name is specifically the ore prefix here
                    OreDictUnifier.onItemRegistration(new OreDictionary.OreRegisterEvent(
                            i.unlocalizedName, i.getStackForm()));
                }
            }
        }

    }

    @BeforeAll
    static void configureEnvironment() {
        SusConfig.DEBUG.enableStoichiometryVerifier = true;
    }

    @BeforeEach
    void resetConfig() {
        SusConfig.DEBUG.stoichiometryRecipeMaps = new String[]{TEST_MAP_NAME};
        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;
    }

    @Test
    void balancedRecipePassesVerification() {
        TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Water.getFluid(1000));

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
    }

    @Test
    void melting() {
        {
            TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                    .input(dust, Zalgonium)
                    .fluidOutputs(Zalgonium.getFluid(GTValues.L));

            assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
        }
        {
            TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                    .input(dust, Zalgonium)
                    .fluidInputs(Water.getFluid(1000))
                    .fluidOutputs(Zalgonium.getFluid(GTValues.L))
                    .fluidOutputs(Ice.getFluid(1000));

            assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
        }
    }

    @Test
    void violationThrowsWhenConfigured() {
        TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(1000));

        assertThrows(RuntimeException.class, () -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
    }

    @Test
    void lossyRecipesAllowMassLoss() {
        TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(1000));

        rb.applyProperty(StoichiometryProperty.getInstance(), StoichiometryProperty.lossy());

        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
    }

    @Test
    void verificationDisabledViaProperty() {
        TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Hydrogen.getFluid(1000));
        rb.applyProperty(StoichiometryProperty.getInstance(), StoichiometryProperty.disableVerifier());

        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
    }

    @Test
    void nonStoichiometricMaterialsAreIgnored() {
        TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Wastewater.getFluid(1000));


        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
    }

    @Test
    void nonStoichiometricMaterialsDontPreventOtherErrors() {
        TestRecipeBuilder rb = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .fluidOutputs(Wastewater.getFluid(1000))
                .fluidOutputs(Water.getFluid(2000));

        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertThrows(RuntimeException.class, () -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
    }

    @Test
    void unknownMaterialsAreProcessed() {
        TestRecipeBuilder rb1 = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .output(dust, Brubium, 1);

        TestRecipeBuilder rb2 = TEST_MAP.recipeBuilder()
                .input(dust, Brubium, 1)
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(1000));

        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb1), TEST_MAP));
        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb2), TEST_MAP));
    }

    @Test
    void unknownMaterialsMayCauseErrors() {
        TestRecipeBuilder rb1 = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .output(dust, Brubium, 1);

        TestRecipeBuilder rb2 = TEST_MAP.recipeBuilder()
                .input(dust, Brubium, 1)
                .fluidOutputs(Hydrogen.getFluid(3000))
                .fluidOutputs(Oxygen.getFluid(1000));

        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb1), TEST_MAP));
        assertThrows(RuntimeException.class, () -> StoichiometryVerifier.verify(groovy(rb2), TEST_MAP));
    }

    @Test
    void dupingRecipesCauseErrors() {
        TestRecipeBuilder rb1 = TEST_MAP.recipeBuilder()
                .fluidInputs(Hydrogen.getFluid(2000))
                .fluidInputs(Oxygen.getFluid(1000))
                .output(dust, Brubium, 1);

        TestRecipeBuilder rb2 = TEST_MAP.recipeBuilder()
                .input(dust, Brubium, 1)
                .fluidOutputs(Hydrogen.getFluid(2000))
                .fluidOutputs(Oxygen.getFluid(1000));

        TestRecipeBuilder rb3 = TEST_MAP.recipeBuilder()
                .input(dust, Brubium, 1)
                .output(dust, Brubium, 2);

        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;

        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb1), TEST_MAP));
        assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(rb2), TEST_MAP));
        assertThrows(RuntimeException.class, () -> StoichiometryVerifier.verify(groovy(rb3), TEST_MAP));
    }

    @Test
    void mixedItemAndFluidRecipes() {
        {
            // Test a balanced recipe with both items and fluids
            TestRecipeBuilder builder = new TestRecipeBuilder()
                    .input(dust, Carbon, 1)
                    .fluidInputs(Hydrogen.getFluid(4500))
                    .fluidOutputs(Methane.getFluid(1000))
                    .chancedFluidOutput(Hydrogen.getFluid(1000), 5000, 0);


            assertDoesNotThrow(() -> StoichiometryVerifier.verify(groovy(builder), TEST_MAP));
        }
        // Test an unbalanced version that should fail
        TestRecipeBuilder builder = new TestRecipeBuilder()
                .input(dust, Carbon, 1)
                .fluidInputs(Hydrogen.getFluid(4500))
                .fluidOutputs(Methane.getFluid(1000))
                .chancedFluidOutput(Hydrogen.getFluid(1000), 500, 0);

        assertThrows(
                UnsupportedClassVersionError.class, // Same as other tests due to test harness
                () -> StoichiometryVerifier.verify(groovy(builder), TEST_MAP)
        );
    }

    @Test
    void checkMoleDecomposition() {
        // H2WO4
        Assertions.assertEquals(7, StoichiometryUtil.getItemsPerMole(TungsticAcid).getNumerator());
        // H2O
        Assertions.assertEquals(3, StoichiometryUtil.getItemsPerMole(Water).getNumerator());

        // Single item moles
        Assertions.assertEquals(1, StoichiometryUtil.getItemsPerMole(BandedIron).getNumerator());
        // Mg(CaCO3)7
        Assertions.assertEquals(36, StoichiometryUtil.getItemsPerMole(Marble).getNumerator());
    }

    private static Recipe groovy(TestRecipeBuilder rb) {
        Recipe recipe = rb.build().getResult();
        try {
            Field field = Recipe.class.getDeclaredField("groovyRecipe");
            field.setAccessible(true);
            field.setBoolean(recipe, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return recipe;
    }

    private static class TestRecipeBuilder extends RecipeBuilder<TestRecipeBuilder> {
        protected TestRecipeBuilder() {
            super();
            this.EUt(1).duration(1);
        }

        public TestRecipeBuilder copy() {
            return new TestRecipeBuilder();
        }
    }

    private static final class TestSidedHandler implements IFMLSidedHandler {

        @Override
        public List<String> getAdditionalBrandingInformation() {
            return Collections.emptyList();
        }

        @Override
        public Side getSide() {
            return Side.SERVER;
        }

        @Override
        public void haltGame(String message, Throwable exception) {
            throw new RuntimeException(message, exception);
        }

        @Override
        public void showGuiScreen(Object clientGuiElement) {
        }

        @Override
        public void queryUser(StartupQuery query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void beginServerLoading(MinecraftServer server) {
        }

        @Override
        public void finishServerLoading() {
        }

        @Override
        public File getSavesDirectory() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MinecraftServer getServer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisplayCloseRequested() {
            return false;
        }

        @Override
        public boolean shouldServerShouldBeKilledQuietly() {
            return false;
        }

        @Override
        public void addModAsResource(ModContainer container) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCurrentLanguage() {
            return "en_US";
        }

        @Override
        public void serverStopped() {
        }

        @Override
        public NetworkManager getClientToServerNetworkManager() {
            throw new UnsupportedOperationException();
        }

        @Override
        public INetHandler getClientPlayHandler() {
            return null;
        }

        @Override
        public void fireNetRegistrationEvent(EventBus bus, NetworkManager manager, Set<String> channelSet,
                                             String channel, Side side) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean shouldAllowPlayerLogins() {
            return false;
        }

        @Override
        public void allowLogins() {
        }

        @Override
        public IThreadListener getWorldThread(INetHandler net) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void processWindowMessages() {
        }

        @Override
        public String stripSpecialChars(String message) {
            return message;
        }

        @Override
        public void reloadRenderers() {
        }

        @Override
        public void fireSidedRegistryEvents() {
        }

        @Override
        public CompoundDataFixer getDataFixer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisplayVSyncForced() {
            return false;
        }
    }


}
