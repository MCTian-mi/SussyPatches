package dev.tianmi.sussypatches.client.renderer.buffer;

import org.lwjgl.opengl.GL30;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record VertexArray(int id) {

    public VertexArray() {
        this(GL30.glGenVertexArrays());
    }

    public void bindVertexArray() {
        GL30.glBindVertexArray(this.id);
    }

    public void unbindVertexArray() {
        GL30.glBindVertexArray(0);
    }

    public void deleteVertexArray() {
        GL30.glDeleteVertexArrays(this.id);
    }
}
