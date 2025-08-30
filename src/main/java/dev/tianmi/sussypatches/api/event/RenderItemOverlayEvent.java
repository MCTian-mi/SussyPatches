package dev.tianmi.sussypatches.api.event;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import mcp.MethodsReturnNonnullByDefault;

@Getter
@Cancelable
@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RenderItemOverlayEvent extends Event {

    protected RenderOperation operation;

    public RenderItemOverlayEvent() {
        reset();
    }

    public void enqueue(RenderOperation operation) {
        this.operation = this.operation.andThen(operation);
    }

    public void reset() {
        this.operation = RenderOperation.EMPTY;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @FunctionalInterface
    public interface RenderOperation {

        // Do nothing, basically.
        RenderOperation EMPTY = (stack, x, y, text) -> {};

        void render(ItemStack stack, int x, int y, @Nullable String text);

        default boolean isEmpty() {
            return this == EMPTY;
        }

        default RenderOperation andThen(RenderOperation other) {
            return (stack, x, y, text) -> {
                this.render(stack, x, y, text);
                other.render(stack, x, y, text);
            };
        }
    }
}
