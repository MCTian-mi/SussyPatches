package dev.tianmi.sussypatches.api.recipes;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.LocalizationUtils;

public class InfoProperty extends RecipeProperty<InfoProperty.TranslationData> {

    public static final String KEY = "info";
    public static InfoProperty INSTANCE;
    private int currentY = 0;

    protected InfoProperty() {
        super(KEY, InfoProperty.TranslationData.class);
    }

    public static InfoProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InfoProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        String translationKey = ((TranslationData) value).getTranslationKey() + ".upfront";
        currentY = y;
        minecraft.fontRenderer.drawString(I18n.hasKey(translationKey) ? I18n.format(translationKey) :
                I18n.format("sussypatches.jei.info.upfront"), x, y, 0x111111);
    }

    @Override
    public void getTooltipStrings(List<String> tooltip, int mouseX, int mouseY, Object value) {
        TranslationData data = (TranslationData) value;
        if (mouseY >= currentY && mouseY <= currentY + 10)
            tooltip.addAll(
                    Arrays.asList(LocalizationUtils.format(data.getTranslationKey(), data.getArgs()).split("\\\\n")));
    }

    public static class TranslationData {

        public String translationKey;
        public Object[] args;

        public TranslationData(String translationKey, Object... args) {
            this.translationKey = translationKey;
            this.args = args;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public Object[] getArgs() {
            return args;
        }
    }
}
