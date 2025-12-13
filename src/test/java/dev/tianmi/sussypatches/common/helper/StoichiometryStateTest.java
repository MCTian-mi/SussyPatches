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

        Reaction reaction = new Reaction(inputs, outputs, true, "Example Reaction");

        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.Creosote, Materials.BacterialSludge));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        Assertions.assertTrue(verifier.addReaction(reaction));

        // Bad reaction
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.BacterialSludge, new Fraction(1));

        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Creosote, new Fraction(2));

        Reaction reaction2 = new Reaction(inputs2, outputs2, true, "Example Reaction");

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

        Reaction reaction = new Reaction(inputs, outputs, true, "Example Reaction");

        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.Creosote, Materials.BacterialSludge));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        Assertions.assertTrue(verifier.addReaction(reaction));

        // Reaction using different materials
        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.BacterialSludge, new Fraction(1));

        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.Aluminium, new Fraction(2));
        outputs2.put(Materials.Dysprosium, new Fraction(1));

        Reaction reaction2 = new Reaction(inputs2, outputs2, true, "Example Reaction");

        Assertions.assertTrue(verifier.addReaction(reaction2));

        Assertions.assertEquals(2, verifier.getState().getComponentCount());

        // Reaction connecting it all together
        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.Aluminium, new Fraction(2));
        inputs3.put(Materials.BacterialSludge, new Fraction(4));

        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Creosote, new Fraction(2));

        Reaction reaction3 = new Reaction(inputs3, outputs3, true, "Example Reaction");

        Assertions.assertTrue(verifier.addReaction(reaction3));
        Assertions.assertEquals(1, verifier.getState().getComponentCount());
    }

    @Test
    void cycle() {
        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.BacterialSludge, Materials.Creosote));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        // Boron -> Creosote -> Bacterial Sludge -> 2 Boron
        Map<Material, Fraction> inputs = new HashMap<>();
        inputs.put(Materials.Boron, new Fraction(1));

        Map<Material, Fraction> outputs = new HashMap<>();
        outputs.put(Materials.Creosote, new Fraction(1));

        Reaction reaction = new Reaction(inputs, outputs, true, "Example Reaction");

        Assertions.assertTrue(verifier.addReaction(reaction));

        Map<Material, Fraction> inputs2 = new HashMap<>();
        inputs2.put(Materials.Creosote, new Fraction(1));

        Map<Material, Fraction> outputs2 = new HashMap<>();
        outputs2.put(Materials.BacterialSludge, new Fraction(1));

        Reaction reaction2 = new Reaction(inputs2, outputs2, true, "Example Reaction");

        Assertions.assertTrue(verifier.addReaction(reaction2));
        Assertions.assertEquals(1, verifier.getState().getComponentCount());

        Map<Material, Fraction> inputs3 = new HashMap<>();
        inputs3.put(Materials.BacterialSludge, new Fraction(1));

        Map<Material, Fraction> outputs3 = new HashMap<>();
        outputs3.put(Materials.Boron, new Fraction(2));

        Reaction reaction3 = new Reaction(inputs3, outputs3, true, "Example Reaction");

        Assertions.assertFalse(verifier.addReaction(reaction3));
        Assertions.assertEquals(1, verifier.getState().getComponentCount());
    }

    @Test
    void hydra() {
        // Boron -> Creosote + Bacterial Sludge

        Set<Material> unknowns = new HashSet<>(Arrays.asList(Materials.BacterialSludge, Materials.Creosote));
        StoichiometryState verifier = new StoichiometryState(unknowns);

        Map<Material, Fraction> inputs = new HashMap<>();
        inputs.put(Materials.Boron, new Fraction(1));

        Map<Material, Fraction> outputs = new HashMap<>();
        outputs.put(Materials.Creosote, new Fraction(1));
        outputs.put(Materials.BacterialSludge, new Fraction(1));

        Reaction reaction = new Reaction(inputs, outputs, true, "Example Reaction");

        Assertions.assertTrue(verifier.addReaction(reaction));
        Assertions.assertEquals(1, verifier.getState().getComponentCount());
    }
}
