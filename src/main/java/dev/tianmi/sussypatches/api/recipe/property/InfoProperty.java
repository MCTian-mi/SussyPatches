package dev.tianmi.sussypatches.api.recipe.property;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.LocalizationUtils;
import lombok.Getter;

public final class InfoProperty extends RecipeProperty<InfoProperty.TranslationData> {

    @Getter(lazy = true)
    private final static InfoProperty Instance = new InfoProperty();
    public static final String KEY = "info";

    private int currentY = 0;

    private InfoProperty() {
        super(KEY, InfoProperty.TranslationData.class);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        String translationKey = castValue(value).translationKey() + ".upfront";
        currentY = y;
        minecraft.fontRenderer.drawString(I18n.hasKey(translationKey) ? I18n.format(translationKey) :
                I18n.format("sussypatches.jei.info.upfront"), x, y, 0x111111);
    }

    @Override
    public void getTooltipStrings(List<String> tooltip, int mouseX, int mouseY, Object value) {
        TranslationData data = castValue(value);
        if (mouseY >= currentY && mouseY <= currentY + 10)
            tooltip.addAll(Arrays.asList(LocalizationUtils.formatLines(data.translationKey(), data.args())));
    }

    public record TranslationData(String translationKey, Object[] args) {}
}
