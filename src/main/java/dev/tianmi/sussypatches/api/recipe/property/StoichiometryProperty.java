package dev.tianmi.sussypatches.api.recipe.property;

import net.minecraft.client.Minecraft;

import gregtech.api.recipes.recipeproperties.RecipeProperty;

/**
 * Stores stoichiometry checking configuration for a recipe.
 */
public final class StoichiometryProperty extends RecipeProperty<StoichiometryProperty.Settings> {

    private static final StoichiometryProperty INSTANCE = new StoichiometryProperty();

    public static final String KEY = "stoichiometry";

    private StoichiometryProperty() {
        super(KEY, Settings.class);
    }

    public static StoichiometryProperty getInstance() {
        return INSTANCE;
    }

    public static final Settings DEFAULT_SETTINGS = new Settings(false, false);

    public static Settings lossy() {
        return new Settings(true, false);
    }

    public static Settings disableVerifier() {
        return new Settings(true, true);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        // no-op: purely internal
    }

    /**
     * Parameters defining how stoichiometry should be handled for a recipe.
     *
     * @param lossy           If {@code true}, loss of mass is allowed during verification.
     * @param disableVerifier If {@code true}, the stoichiometry verifier is completely disabled for the recipe.
     */
    public record Settings(boolean lossy, boolean disableVerifier) {}
}
