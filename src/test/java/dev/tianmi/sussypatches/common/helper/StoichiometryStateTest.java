package dev.tianmi.sussypatches.common.helper;

import dev.tianmi.sussypatches.common.stoichiometry.Reaction;
import dev.tianmi.sussypatches.common.stoichiometry.StoichiometryState;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import org.apache.commons.math3.fraction.Fraction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

public class StoichiometryStateTest {
    @BeforeAll
    static void configureEnvironment() {
        Bootstrap.init();
    }

    @Test
    void basic() {
        Map<Material, Fraction> inputs = new HashMap<>();
        inputs.put(Materials.Creosote, new Fraction(3));
        inputs.put(Materials.Boron, new Fraction(2));

        Map<Material, Fraction> outputs = new HashMap<>();
        outputs.put(Materials.BacterialSludge, new Fraction(5));
        outputs.put(Materials.Carbon, new Fraction(2));

        Reaction reaction = new Reaction(inputs, outputs, true, "Creosote to Bacterial Sludge");

        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.Creosote, Materials.BacterialSludge));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        Assertions.assertTrue(verifier.addReaction(reaction));

        // Bad reaction
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.BacterialSludge, new Fraction(1));

        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Creosote, new Fraction(2));

        Reaction reaction2 = new Reaction(inputs2, outputs2, true, "Duping Creosote");

        Assertions.assertFalse(verifier.addReaction(reaction2));

        System.out.println(verifier.getState()); // Debugging info
    }

    @Test
    void testSeparation() {
        Map<Material, Fraction> inputs = new HashMap<>();
        inputs.put(Materials.Creosote, new Fraction(3));
        inputs.put(Materials.Boron, new Fraction(2));

        Map<Material, Fraction> outputs = new HashMap<>();
        outputs.put(Materials.BacterialSludge, new Fraction(5));
        outputs.put(Materials.Carbon, new Fraction(2));

        Reaction reaction = new Reaction(inputs, outputs, true, "Creosote to Bacterial Sludge");

        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.Creosote, Materials.BacterialSludge, Materials.DarkAsh));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        Assertions.assertTrue(verifier.addReaction(reaction));

        // Reaction using different materials
        Map<Material, Fraction> inputs2 = new HashMap<>();
        Map<Material, Fraction> outputs2 = new HashMap<>();

        outputs2.put(Materials.DarkAsh, new Fraction(1));

        inputs2.put(Materials.Fluorine, new Fraction(2));
        inputs2.put(Materials.Hydrogen, new Fraction(1));

        Reaction reaction2 = new Reaction(inputs2, outputs2, true, "HF -> Dark Ash");

        Assertions.assertTrue(verifier.addReaction(reaction2));

        Assertions.assertEquals(2, verifier.getState().getGroupCount());
        Assertions.assertEquals(6, verifier.getState().getVariableCount());

        // Reaction connecting it all together
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Fluorine, new Fraction(2));
        inputs3.put(Materials.DarkAsh, new Fraction(4));

        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Creosote, new Fraction(2));

        Reaction reaction3 = new Reaction(inputs3, outputs3, true, "Fluorine + Dark Ash -> Creosote");

        // Carbon is now made from thin air!
        Assertions.assertFalse(verifier.addReaction(reaction3));
        Assertions.assertEquals(1, verifier.getState().getGroupCount());
        Assertions.assertEquals(12, verifier.getState().getVariableCount());
    }

    @Test
    void testSimpleChain() {
        // B -> X -> Z -> 2B should fail (matter duplication)
        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.RocketFuel, Materials.Glue));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, new Fraction(1));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs1, outputs1, true, "B->Fuel")));

        // RocketFuel -> Glue
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, new Fraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs2, outputs2, true, "Fuel->Glue")));

        // Glue -> 2B (should fail!)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, new Fraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, new Fraction(2));
        Assertions.assertFalse(verifier.addReaction(
                new Reaction(inputs3, outputs3, true, "Glue->2B")));
    }

    @Test
    void testValidChain() {
        // 2B -> X -> Z -> B should pass (conserves matter)
        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.RocketFuel, Materials.Glue));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // 2B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, new Fraction(2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs1, outputs1, true, "2B->Fuel")));

        // RocketFuel -> Glue
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, new Fraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs2, outputs2, true, "Fuel->Glue")));

        // Glue -> B (should pass)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, new Fraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs3, outputs3, true, "Glue->B")));
    }

    @Test
    void testMultipleElements() {
        // Test with multiple elements tracking independently
        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.Lubricant, Materials.McGuffium239));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // 3B + 2C -> Lubricant
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, new Fraction(3));
        inputs1.put(Materials.Carbon, new Fraction(2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.Lubricant, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs1, outputs1, true, "B+C->Lube")));

        // Lubricant -> McGuffium239
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.Lubricant, new Fraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.McGuffium239, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs2, outputs2, true, "Lube->McG")));

        // McGuffium239 -> 4B + C (should fail - too much boron)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.McGuffium239, new Fraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, new Fraction(4));
        outputs3.put(Materials.Carbon, new Fraction(1));
        Assertions.assertFalse(verifier.addReaction(
                new Reaction(inputs3, outputs3, true, "McG->4B+C")));
    }

    @Test
    void testBranching() {
        // Test branching chains with multiple paths
        Set<Material> unknowns = new HashSet<>(Arrays.asList(
                Materials.RocketFuel, Materials.Glue, Materials.Lubricant, Materials.DarkAsh));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // 10B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, new Fraction(10));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs1, outputs1, true, "10B->Fuel")));

        // RocketFuel -> Glue + Lubricant
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, new Fraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, new Fraction(1));
        outputs2.put(Materials.Lubricant, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs2, outputs2, true, "Fuel->Glue+Lube")));

        // Glue -> 3B
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, new Fraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, new Fraction(3));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs3, outputs3, true, "Glue->3B")));

        // Lubricant -> 8B (should fail - only 7B left)
        Map<Material, Fraction> inputs4 = new HashMap<>();
        inputs4.put(Materials.Lubricant, new Fraction(1));
        Map<Material, Fraction> outputs4 = new HashMap<>();
        outputs4.put(Materials.Boron, new Fraction(8));
        Assertions.assertFalse(verifier.addReaction(
                new Reaction(inputs4, outputs4, true, "Lube->8B")));
    }

    @Test
    void testFractionalCoefficients() {
        // Test with fractional stoichiometry
        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.RocketFuel));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // 3/2 B -> RocketFuel
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, new Fraction(3, 2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs1, outputs1, true, "1.5B->Fuel")));

        // RocketFuel -> 2B (should fail)
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, new Fraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Boron, new Fraction(2));
        Assertions.assertFalse(verifier.addReaction(
                new Reaction(inputs2, outputs2, true, "Fuel->2B")));

        // But RocketFuel -> B should pass
        Set<Material> unknowns2 = new HashSet<>(Arrays.asList(Materials.Glue));
        StoichiometryState verifier2 = new StoichiometryState(unknowns2);

        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Boron, new Fraction(3, 2));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Glue, new Fraction(1));
        Assertions.assertTrue(verifier2.addReaction(
                new Reaction(inputs3, outputs3, true, "1.5B->Glue")));

        Map<Material, Fraction> inputs4 = new HashMap<>();
        inputs4.put(Materials.Glue, new Fraction(1));
        Map<Material, Fraction> outputs4 = new HashMap<>();
        outputs4.put(Materials.Boron, new Fraction(1));
        Assertions.assertTrue(verifier2.addReaction(
                new Reaction(inputs4, outputs4, true, "Glue->B")));
    }

    @Test
    void testComplexMerge() {
        // Test merging of three separate groups
        Set<Material> unknowns = new HashSet<>(Arrays.asList(
                Materials.RocketFuel, Materials.Glue, Materials.Lubricant,
                Materials.DarkAsh, Materials.BacterialSludge, Materials.Creosote));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // Group 1: Boron chain
        Map<Material, Fraction> in1 = new HashMap<>();
        in1.put(Materials.Boron, new Fraction(5));
        Map<Material, Fraction> out1 = new HashMap<>();
        out1.put(Materials.RocketFuel, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in1, out1, true, "5B->Fuel")));

        Map<Material, Fraction> in2 = new HashMap<>();
        in2.put(Materials.RocketFuel, new Fraction(1));
        Map<Material, Fraction> out2 = new HashMap<>();
        out2.put(Materials.Glue, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in2, out2, true, "Fuel->Glue")));

        // Group 2: Carbon chain
        Map<Material, Fraction> in3 = new HashMap<>();
        in3.put(Materials.Carbon, new Fraction(3));
        Map<Material, Fraction> out3 = new HashMap<>();
        out3.put(Materials.Lubricant, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in3, out3, true, "3C->Lube")));

        Map<Material, Fraction> in4 = new HashMap<>();
        in4.put(Materials.Lubricant, new Fraction(1));
        Map<Material, Fraction> out4 = new HashMap<>();
        out4.put(Materials.DarkAsh, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in4, out4, true, "Lube->Ash")));

        // Group 3: Hydrogen chain
        Map<Material, Fraction> in5 = new HashMap<>();
        in5.put(Materials.Hydrogen, new Fraction(7));
        Map<Material, Fraction> out5 = new HashMap<>();
        out5.put(Materials.BacterialSludge, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in5, out5, true, "7H->Sludge")));

        Map<Material, Fraction> in6 = new HashMap<>();
        in6.put(Materials.BacterialSludge, new Fraction(1));
        Map<Material, Fraction> out6 = new HashMap<>();
        out6.put(Materials.Creosote, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in6, out6, true, "Sludge->Creo")));

        // Should have 3 groups
        Assertions.assertEquals(3, verifier.getState().getGroupCount());

        // Merge groups 1 and 2
        Map<Material, Fraction> in7 = new HashMap<>();
        in7.put(Materials.Glue, new Fraction(1));
        in7.put(Materials.DarkAsh, new Fraction(1));
        Map<Material, Fraction> out7 = new HashMap<>();
        out7.put(Materials.Boron, new Fraction(2));
        out7.put(Materials.Carbon, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in7, out7, true, "Glue+Ash->2B+C")));

        // Should have 2 groups now
        Assertions.assertEquals(2, verifier.getState().getGroupCount());

        // Merge all three groups
        Map<Material, Fraction> in8 = new HashMap<>();
        in8.put(Materials.Creosote, new Fraction(1));
        in8.put(Materials.DarkAsh, new Fraction(1));
        Map<Material, Fraction> out8 = new HashMap<>();
        out8.put(Materials.Hydrogen, new Fraction(3));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(in8, out8, true, "Creo+Ash->3H")));

        // Should have 1 group now
        Assertions.assertEquals(1, verifier.getState().getGroupCount());

        // Try to create matter from nothing - should fail
        Map<Material, Fraction> in9 = new HashMap<>();
        in9.put(Materials.Glue, new Fraction(1));
        Map<Material, Fraction> out9 = new HashMap<>();
        out9.put(Materials.Hydrogen, new Fraction(10));
        Assertions.assertFalse(verifier.addReaction(
                new Reaction(in9, out9, true, "Glue->10H")));
    }

    @Test
    void testEqualityConstraints() {
        // Test with perfect (equality) reactions instead of lossy
        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.RocketFuel, Materials.Glue));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // 2B -> RocketFuel (perfect reaction)
        Map<Material, Fraction> inputs1 = new HashMap<>();
        inputs1.put(Materials.Boron, new Fraction(2));
        Map<Material, Fraction> outputs1 = new HashMap<>();
        outputs1.put(Materials.RocketFuel, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs1, outputs1, false, "2B->Fuel (perfect)")));

        // RocketFuel -> Glue (perfect reaction)
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.RocketFuel, new Fraction(1));
        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Glue, new Fraction(1));
        Assertions.assertTrue(verifier.addReaction(
                new Reaction(inputs2, outputs2, false, "Fuel->Glue (perfect)")));

        // Glue -> B (should fail - only 2B available, trying to get 1B means RocketFuel has 1B)
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Glue, new Fraction(1));
        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, new Fraction(1));
        Assertions.assertFalse(verifier.addReaction(
                new Reaction(inputs3, outputs3, false, "Glue->B (perfect)")));

        // But Glue -> 2B should pass (exact conservation)
        Set<Material> unknowns2 = new HashSet<>(Arrays.asList(Materials.Lubricant, Materials.DarkAsh));
        StoichiometryState verifier2 = new StoichiometryState(unknowns2);

        Map<Material, Fraction> inputs4 = new HashMap<>();
        inputs4.put(Materials.Boron, new Fraction(2));
        Map<Material, Fraction> outputs4 = new HashMap<>();
        outputs4.put(Materials.Lubricant, new Fraction(1));
        Assertions.assertTrue(verifier2.addReaction(
                new Reaction(inputs4, outputs4, false, "2B->Lube (perfect)")));

        Map<Material, Fraction> inputs5 = new HashMap<>();
        inputs5.put(Materials.Lubricant, new Fraction(1));
        Map<Material, Fraction> outputs5 = new HashMap<>();
        outputs5.put(Materials.DarkAsh, new Fraction(1));
        Assertions.assertTrue(verifier2.addReaction(
                new Reaction(inputs5, outputs5, false, "Lube->Ash (perfect)")));

        Map<Material, Fraction> inputs6 = new HashMap<>();
        inputs6.put(Materials.DarkAsh, new Fraction(1));
        Map<Material, Fraction> outputs6 = new HashMap<>();
        outputs6.put(Materials.Boron, new Fraction(2));
        Assertions.assertTrue(verifier2.addReaction(
                new Reaction(inputs6, outputs6, false, "Ash->2B (perfect)")));
    }
}