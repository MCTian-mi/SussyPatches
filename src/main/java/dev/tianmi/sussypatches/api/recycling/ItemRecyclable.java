package dev.tianmi.sussypatches.api.recycling;

import dev.tianmi.sussypatches.api.util.ItemAndMeta;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NullMarked;

/// A concrete item node, identified by [ItemAndMeta] (which also covers the `GTValues.W` wildcard meta).
@NullMarked
public record ItemRecyclable(ItemAndMeta itemAndMetadata) implements Recyclable {

    public ItemRecyclable(ItemStack stack) {
        this(new ItemAndMeta(stack));
    }

    public ItemRecyclable(Item item) {
        this(item, 0);
    }

    public ItemRecyclable(Item item, int damage) {
        this(new ItemAndMeta(item, damage));
    }

    public ItemRecyclable(Block block) {
        this(new ItemAndMeta(new ItemStack(block)));
    }

    @Override
    public ItemStack asStack(int size) {
        return itemAndMetadata.asStack(size);
    }

    @Override
    public String toString() {
        return "I[" + itemAndMetadata + "]";
    }
}
