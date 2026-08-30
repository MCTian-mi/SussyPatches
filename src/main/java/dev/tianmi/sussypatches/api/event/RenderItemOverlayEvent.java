package dev.tianmi.sussypatches.api.event;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@Getter
@NullMarked
@Cancelable
@SideOnly(Side.CLIENT)
public class RenderItemOverlayEvent extends Event {

    public static final RenderItemOverlayEvent INSTANCE = new RenderItemOverlayEvent();

    protected RenderOperation operation = RenderOperation.EMPTY;

    @ApiStatus.Internal
    public RenderItemOverlayEvent() {
    }

    public void enqueue(RenderOperation operation) {
        this.operation = this.operation.andThen(operation);
    }

    public void reset() {
        this.operation = RenderOperation.EMPTY;
        this.phase = null;
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
