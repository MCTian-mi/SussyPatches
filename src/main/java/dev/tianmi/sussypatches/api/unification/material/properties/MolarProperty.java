package dev.tianmi.sussypatches.api.unification.material.properties;

import dev.tianmi.sussypatches.common.stoichiometry.apachemath.fraction.Fraction;
import gregtech.api.GTValues;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;

public class MolarProperty implements IMaterialProperty {

    public static final PropertyKey<MolarProperty> MOLAR = new PropertyKey<>("molar", MolarProperty.class);

    public final Fraction itemToMole;
    public final Fraction fluidToMole;

    private MolarProperty(Fraction itemToMole, Fraction fluidToMole) {
        this.itemToMole = itemToMole;
        this.fluidToMole = fluidToMole;
    }

    public static MolarProperty fromItemConversion(int itemToMole) {
        return fromItemConversion(itemToMole, GTValues.L);
    }

    public static MolarProperty fromItemConversion(int itemToMole, int fluidToItem) {
        return new MolarProperty(new Fraction(itemToMole, 1), new Fraction(itemToMole * fluidToItem, 1));
    }

    public static MolarProperty fromFluidConversion(int fluidToMole) {
        return fromFluidConversion(fluidToMole, GTValues.L);
    }

    public static MolarProperty fromFluidConversion(int fluidToMole, int fluidToItem) {
        return new MolarProperty(new Fraction(fluidToMole, fluidToItem), new Fraction(fluidToMole, 1));
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}
}
