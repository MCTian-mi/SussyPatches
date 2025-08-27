package dev.tianmi.sussypatches.api.recipes;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.Arrays;
import java.util.List;

public class InfoProperty extends RecipeProperty<String> {
    public static final String KEY = "info";
    public static InfoProperty INSTANCE;
    private int currentY = 0;

    protected InfoProperty() {
        super(KEY, String.class);
    }

    public static InfoProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InfoProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        String translationKey = value + ".upfront";
        currentY = y;
        minecraft.fontRenderer.drawString(I18n.hasKey(translationKey) ? I18n.format(translationKey) : I18n.format("sussypatches.jei.info.upfront"), x, y, 0x111111);
    }

    @Override
    public void getTooltipStrings(List<String> tooltip, int mouseX, int mouseY, Object value) {
        if (mouseY >= currentY && mouseY <= currentY + 10)
            tooltip.addAll(Arrays.asList(LocalizationUtils.format((String) value).split("\\\\n")));
    }
}
