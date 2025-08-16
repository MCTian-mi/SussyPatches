package dev.tianmi.sussypatches.client.renderer.buffer;

import org.lwjgl.opengl.GL30;

public class VertexArrayObject {

    protected int id;

    public VertexArrayObject() {
        this.id = GL30.glGenVertexArrays();
    }

    public void bindVertexArray() {
        GL30.glBindVertexArray(this.id);
    }

    public void unbindVertexArray() {
        GL30.glBindVertexArray(0);
    }

    public void deleteVertexArray() {
        if (this.id > 0) {
            GL30.glDeleteVertexArrays(this.id);
        }
        this.id = -1;
    }
}
