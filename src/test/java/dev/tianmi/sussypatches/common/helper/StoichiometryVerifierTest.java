package dev.tianmi.sussypatches.common.helper;

import static dev.tianmi.sussypatches.common.helper.Bootstrap.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;

import net.minecraftforge.fml.common.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.tianmi.sussypatches.api.recipe.property.StoichiometryProperty;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.common.stoichiometry.StoichiometryUtil;
import dev.tianmi.sussypatches.common.stoichiometry.StoichiometryVerifier;
import dev.tianmi.sussypatches.common.stoichiometry.StoichiometryViolationException;
import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;

public class StoichiometryVerifierTest {

    @BeforeAll
    static void configureEnvironment() {
        Bootstrap.init();
        SusConfig.DEBUG.enableStoichiometryVerifier = true;
    }

    @BeforeEach
    void resetConfig() {
        SusConfig.DEBUG.stoichiometryRecipeMaps = new String[] { TEST_MAP_NAME };
        SusConfig.DEBUG.stoichiometryThrowOnViolation = true;
        StoichiometryVerifier.stoichiometryState.clear();
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

        assertThrows(StoichiometryViolationException.class, () -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
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

        assertThrows(StoichiometryViolationException.class, () -> StoichiometryVerifier.verify(groovy(rb), TEST_MAP));
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
        assertThrows(StoichiometryViolationException.class, () -> StoichiometryVerifier.verify(groovy(rb2), TEST_MAP));
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
        assertThrows(StoichiometryViolationException.class, () -> StoichiometryVerifier.verify(groovy(rb3), TEST_MAP));
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
                StoichiometryViolationException.class,
                () -> StoichiometryVerifier.verify(groovy(builder), TEST_MAP));
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
}
