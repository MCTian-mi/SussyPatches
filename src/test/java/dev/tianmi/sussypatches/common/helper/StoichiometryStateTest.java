package dev.tianmi.sussypatches.common.helper;

import java.util.*;

import org.apache.commons.lang3.math.Fraction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dev.tianmi.sussypatches.common.stoichiometry.StoichiometryState;
import dev.tianmi.sussypatches.common.stoichiometry.StoichiometryViolationException;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;

public class StoichiometryStateTest {

    @BeforeAll
    static void configureEnvironment() {
        Bootstrap.init();
    }

    @Test
    void basic() {
        Map<Material, Fraction> inputs = new HashMap<>();
        inputs.put(Materials.Creosote, Fraction.getFraction(3));
        inputs.put(Materials.Boron, Fraction.getFraction(2));

        Map<Material, Fraction> outputs = new HashMap<>();
        outputs.put(Materials.BacterialSludge, Fraction.getFraction(5));
        outputs.put(Materials.Carbon, Fraction.getFraction(2));

        StoichiometryState verifier = new StoichiometryState();

        Assertions.assertTrue(verifier.addReaction(inputs, outputs, true));

        // Bad reaction
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.BacterialSludge, Fraction.getFraction(1));

        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Creosote, Fraction.getFraction(2));

        Assertions.assertThrows(StoichiometryViolationException.class,
                () -> verifier.addReaction(inputs2, outputs2, true));

        System.out.println(verifier.getState()); // Debugging info
    }

    @Test
    void testSeparation() {
        Map<Material, Fraction> inputs = new HashMap<>();
        inputs.put(Materials.Creosote, Fraction.getFraction(3));
        inputs.put(Materials.Boron, Fraction.getFraction(2));

        Map<Material, Fraction> outputs = new HashMap<>();
        outputs.put(Materials.BacterialSludge, Fraction.getFraction(5));
        outputs.put(Materials.Carbon, Fraction.getFraction(2));

        StoichiometryState verifier = new StoichiometryState();

        Assertions.assertTrue(verifier.addReaction(inputs, outputs, true));

        // Reaction using different materials
        Map<Material, Fraction> inputs2 = new HashMap<>();
        Map<Material, Fraction> outputs2 = new HashMap<>();

        outputs2.put(Materials.DarkAsh, Fraction.getFraction(1));

        inputs2.put(Materials.Fluorine, Fraction.getFraction(2));
        inputs2.put(Materials.Hydrogen, Fraction.getFraction(1));

        Assertions.assertTrue(verifier.addReaction(inputs2, outputs2, true));

        Assertions.assertEquals(2, verifier.getState().getGroupCount());
        Assertions.assertEquals(6, verifier.getState().getVariableCount());

        // Reaction connecting it all together
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Fluorine, Fraction.getFraction(2));
        inputs3.put(Materials.DarkAsh, Fraction.getFraction(4));

        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Creosote, Fraction.getFraction(2));

        // Carbon is now made from thin air!
        Assertions.assertThrows(StoichiometryViolationException.class,
                () -> verifier.addReaction(inputs3, outputs3, true));
        Assertions.assertEquals(1, verifier.getState().getGroupCount());
        Assertions.assertEquals(12, verifier.getState().getVariableCount());
    }

    @Test
    void testSimpleChain() {
        // B -> X -> Z -> 2B should fail (matter duplication)
        StoichiometryState verifier = new StoichiometryState();

        // B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, Fraction.getFraction(1));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(
                inputs1, outputs1, true));

        // RocketFuel -> Glue
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, Fraction.getFraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs2, outputs2, true));

        // Glue -> 2B (should fail!)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, Fraction.getFraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, Fraction.getFraction(2));
        Assertions.assertThrows(StoichiometryViolationException.class,
                () -> verifier.addReaction(inputs3, outputs3, true));
    }

    @Test
    void testValidChain() {
        // 2B -> X -> Z -> B should pass (conserves matter)
        StoichiometryState verifier = new StoichiometryState();

        // 2B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, Fraction.getFraction(2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs1, outputs1, true));

        // RocketFuel -> Glue
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, Fraction.getFraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(
                inputs2, outputs2, true));

        // Glue -> B (should pass)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, Fraction.getFraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs3, outputs3, true));
    }

    @Test
    void testMultipleElements() {
        // Test with multiple elements tracking independently
        StoichiometryState verifier = new StoichiometryState();

        // 3B + 2C -> Lubricant
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, Fraction.getFraction(3));
        inputs1.put(Materials.Carbon, Fraction.getFraction(2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.Lubricant, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs1, outputs1, true));

        // Lubricant -> McGuffium239
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.Lubricant, Fraction.getFraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.McGuffium239, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs2, outputs2, true));

        // McGuffium239 -> 4B + C (should fail - too much boron)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.McGuffium239, Fraction.getFraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, Fraction.getFraction(4));
        outputs3.put(Materials.Carbon, Fraction.getFraction(1));
        Assertions.assertThrows(StoichiometryViolationException.class,
                () -> verifier.addReaction(inputs3, outputs3, true));
    }

    @Test
    void testBranching() {
        // Test branching chains with multiple paths
        StoichiometryState verifier = new StoichiometryState();

        // 10B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, Fraction.getFraction(10));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs1, outputs1, true));

        // RocketFuel -> Glue + Lubricant
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, Fraction.getFraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, Fraction.getFraction(1));
        outputs2.put(Materials.Lubricant, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs2, outputs2, true));

        // Glue -> 3B
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, Fraction.getFraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, Fraction.getFraction(3));
        Assertions.assertTrue(verifier.addReaction(inputs3, outputs3, true));

        // Lubricant -> 8B (should fail - only 7B left)
        Map<Material, Fraction> inputs4 = new HashMap<>();
        inputs4.put(Materials.Lubricant, Fraction.getFraction(1));
        Map<Material, Fraction> outputs4 = new HashMap<>();
        outputs4.put(Materials.Boron, Fraction.getFraction(8));
        Assertions.assertThrows(StoichiometryViolationException.class,
                () -> verifier.addReaction(inputs4, outputs4, true));
    }

    @Test
    void testFractionalCoefficients() {
        // Test with fractional stoichiometry
        StoichiometryState verifier = new StoichiometryState();

        // 3/2 B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, Fraction.getFraction(3, 2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(inputs1, outputs1, true));

        // RocketFuel -> 2B (should fail)
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, Fraction.getFraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Boron, Fraction.getFraction(2));
        Assertions.assertThrows(StoichiometryViolationException.class,
                () -> verifier.addReaction(inputs2, outputs2, true));

        // But RocketFuel -> B should pass
        StoichiometryState verifier2 = new StoichiometryState();

        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Boron, Fraction.getFraction(3, 2));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Glue, Fraction.getFraction(1));
        Assertions.assertTrue(verifier2.addReaction(inputs3, outputs3, true));

        Map<Material, Fraction> inputs4 = new HashMap<>();
        inputs4.put(Materials.Glue, Fraction.getFraction(1));
        Map<Material, Fraction> outputs4 = new HashMap<>();
        outputs4.put(Materials.Boron, Fraction.getFraction(1));
        Assertions.assertTrue(verifier2.addReaction(inputs4, outputs4, true));
    }

    @Test
    void testComplexMerge() {
        // Test merging of three separate groups
        StoichiometryState verifier = new StoichiometryState();

        // Group 1: Boron chain
        Map<Material, Fraction> in1 = new HashMap<>();
        in1.put(Materials.Boron, Fraction.getFraction(5));
        Map<Material, Fraction> out1 = new HashMap<>();
        out1.put(Materials.RocketFuel, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(in1, out1, true));

        Map<Material, Fraction> in2 = new HashMap<>();
        in2.put(Materials.RocketFuel, Fraction.getFraction(1));
        Map<Material, Fraction> out2 = new HashMap<>();
        out2.put(Materials.Glue, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(in2, out2, true));

        // Group 2: Carbon chain
        Map<Material, Fraction> in3 = new HashMap<>();
        in3.put(Materials.Carbon, Fraction.getFraction(3));
        Map<Material, Fraction> out3 = new HashMap<>();
        out3.put(Materials.Lubricant, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(in3, out3, true));

        Map<Material, Fraction> in4 = new HashMap<>();
        in4.put(Materials.Lubricant, Fraction.getFraction(1));
        Map<Material, Fraction> out4 = new HashMap<>();
        out4.put(Materials.DarkAsh, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(in4, out4, true));

        // Group 3: Hydrogen chain
        Map<Material, Fraction> in5 = new HashMap<>();
        in5.put(Materials.Hydrogen, Fraction.getFraction(7));
        Map<Material, Fraction> out5 = new HashMap<>();
        out5.put(Materials.BacterialSludge, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(
                in5, out5, true));

        Map<Material, Fraction> in6 = new HashMap<>();
        in6.put(Materials.BacterialSludge, Fraction.getFraction(1));
        Map<Material, Fraction> out6 = new HashMap<>();
        out6.put(Materials.Creosote, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(
                in6, out6, true));

        // Should have 3 groups
        Assertions.assertEquals(3, verifier.getState().getGroupCount());

        // Merge groups 1 and 2
        Map<Material, Fraction> in7 = new HashMap<>();
        in7.put(Materials.Glue, Fraction.getFraction(1));
        in7.put(Materials.DarkAsh, Fraction.getFraction(1));
        Map<Material, Fraction> out7 = new HashMap<>();
        out7.put(Materials.Boron, Fraction.getFraction(2));
        out7.put(Materials.Carbon, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(
                in7, out7, true));

        // Should have 2 groups now
        Assertions.assertEquals(2, verifier.getState().getGroupCount());

        // Merge all three groups
        Map<Material, Fraction> in8 = new HashMap<>();
        in8.put(Materials.Creosote, Fraction.getFraction(1));
        in8.put(Materials.DarkAsh, Fraction.getFraction(1));
        Map<Material, Fraction> out8 = new HashMap<>();
        out8.put(Materials.Hydrogen, Fraction.getFraction(3));
        Assertions.assertTrue(verifier.addReaction(
                in8, out8, true));

        // Should have 1 group now
        Assertions.assertEquals(1, verifier.getState().getGroupCount());

        // Try to create matter from nothing - should fail
        Map<Material, Fraction> in9 = new HashMap<>();
        in9.put(Materials.Glue, Fraction.getFraction(1));
        Map<Material, Fraction> out9 = new HashMap<>();
        out9.put(Materials.Hydrogen, Fraction.getFraction(10));
        Assertions.assertThrows(StoichiometryViolationException.class, () -> verifier.addReaction(
                in9, out9, true));
    }

    @Test
    void testEqualityConstraints() {
        // Test with perfect (equality) reactions instead of lossy
        StoichiometryState verifier = new StoichiometryState();

        // 2B -> RocketFuel (perfect reaction)
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, Fraction.getFraction(2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(
                inputs1, outputs1, false));

        // RocketFuel -> Glue (perfect reaction)
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, Fraction.getFraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, Fraction.getFraction(1));
        Assertions.assertTrue(verifier.addReaction(
                inputs2, outputs2, false));

        // Glue -> B (should fail - only 2B available, trying to get 1B means RocketFuel has 1B)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, Fraction.getFraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, Fraction.getFraction(1));
        Assertions.assertThrows(StoichiometryViolationException.class, () -> verifier.addReaction(
                inputs3, outputs3, false));

        // But Glue -> 2B should pass (exact conservation)
        StoichiometryState verifier2 = new StoichiometryState();

        Map<Material, Fraction> inputs4 = new HashMap<>();
        inputs4.put(Materials.Boron, Fraction.getFraction(2));
        Map<Material, Fraction> outputs4 = new HashMap<>();
        outputs4.put(Materials.Lubricant, Fraction.getFraction(1));
        Assertions.assertTrue(verifier2.addReaction(
                inputs4, outputs4, false));

        Map<Material, Fraction> inputs5 = new HashMap<>();
        inputs5.put(Materials.Lubricant, Fraction.getFraction(1));
        Map<Material, Fraction> outputs5 = new HashMap<>();
        outputs5.put(Materials.DarkAsh, Fraction.getFraction(1));
        Assertions.assertTrue(verifier2.addReaction(
                inputs5, outputs5, false));

        Map<Material, Fraction> inputs6 = new HashMap<>();
        inputs6.put(Materials.DarkAsh, Fraction.getFraction(1));
        Map<Material, Fraction> outputs6 = new HashMap<>();
        outputs6.put(Materials.Boron, Fraction.getFraction(2));
        Assertions.assertTrue(verifier2.addReaction(
                inputs6, outputs6, false));
    }
}
