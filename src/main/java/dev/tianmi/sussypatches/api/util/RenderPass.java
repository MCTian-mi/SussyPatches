package dev.tianmi.sussypatches.api.util;

public enum RenderPass {

    NORMAL,
    TRANSLUCENT;

    public boolean isTranslucent() {
        return this == TRANSLUCENT;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }
}
