package dev.tianmi.sussypatches.api.util;

import org.lwjgl.opengl.GL30;

public class OpenGL3Helper {

    public static boolean gl3 = false;
    public static boolean checked = false;

    public static boolean isGl3Loaded() {
        if (!checked) {
            gl3 = SusMods.Cleanroom.isLoaded() || SusMods.Lwjgl3ify.isLoaded();
            checked = true;
        }
        return gl3;
    }

    public static int genVertexArrays() {
        return GL30.glGenVertexArrays();
    }

    public static void bindVertexArray(int array) {
        GL30.glBindVertexArray(array);
    }
}
