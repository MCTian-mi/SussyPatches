package dev.tianmi.sussypatches.api.unification.material.properties;

import gregtech.api.GTValues;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import org.apache.commons.lang3.math.Fraction;

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
        return new MolarProperty(Fraction.getFraction(itemToMole, 1), Fraction.getFraction(itemToMole * fluidToItem, 1));
    }

    public static MolarProperty fromFluidConversion(int fluidToMole) {
        return fromFluidConversion(fluidToMole, GTValues.L);
    }

    public static MolarProperty fromFluidConversion(int fluidToMole, int fluidToItem) {
        return new MolarProperty(Fraction.getFraction(fluidToMole, fluidToItem), Fraction.getFraction(fluidToMole, 1));
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}
}
