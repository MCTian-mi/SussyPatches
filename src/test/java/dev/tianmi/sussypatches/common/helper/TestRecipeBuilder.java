package dev.tianmi.sussypatches.common.helper;

import gregtech.api.recipes.RecipeBuilder;

class TestRecipeBuilder extends RecipeBuilder<TestRecipeBuilder> {
    protected TestRecipeBuilder() {
        super();
        this.EUt(1).duration(1);
    }

    public TestRecipeBuilder copy() {
        return new TestRecipeBuilder();
    }
}
