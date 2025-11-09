package dev.tianmi.sussypatches.api.util;

import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import dev.tianmi.sussypatches.api.util.Result.Err;
import dev.tianmi.sussypatches.api.util.Result.Ok;
import gregtech.api.GTValues;
import mcp.MethodsReturnNonnullByDefault;

/// A re-implementation of [ItemAndMetadata] but more powerful
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record ItemAndMeta(Item item, int meta) implements Predicate<ItemStack> {

    public static final ItemAndMeta EMPTY = new ItemAndMeta(ItemStack.EMPTY);

    public static Result<ItemAndMeta, IllegalArgumentException> fromString(String s) {
        Objects.requireNonNull(s, "input");
        String input = s.trim();
        if (input.isBlank()) return new Err<>(new IllegalArgumentException("Blank string"));

        int parsedMeta = 0;
        int at = input.lastIndexOf('@');
        if (at >= 0) {
            String metaPart = input.substring(at + 1).trim();
            if ("*".equals(metaPart)) {
                parsedMeta = GTValues.W;
            } else {
                try {
                    parsedMeta = Integer.parseInt(metaPart);
                } catch (NumberFormatException e) {
                    return new Err<>(new IllegalArgumentException("Invalid meta value: " + metaPart, e));
                }
            }
            input = input.substring(0, at).trim();
        }

        if (input.isEmpty()) return new Err<>(new IllegalArgumentException("Missing item id in input: " + s));

        Item found = Item.getByNameOrId(input);
        if (found == null) return new Err<>(new IllegalArgumentException("Unknown item id: " + input));

        return new Ok<>(new ItemAndMeta(found, parsedMeta));
    }

    public ItemAndMeta(ItemStack stack) {
        this(stack.getItem(), stack.getMetadata());
    }

    public ItemAndMeta(Item item) {
        this(item, 0);
    }

    public ItemStack asStack(int size) {
        return new ItemStack(this.item, size, this.isWildcard() ? 0 : this.meta);
    }

    public ItemStack asStack() {
        return asStack(1);
    }

    public boolean isWildcard() {
        return this.meta == GTValues.W;
    }

    public ItemAndMeta asWildcard() {
        return new ItemAndMeta(this.item, GTValues.W);
    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + meta;
        return result;
    }

    @Override
    public String toString() {
        return item.getRegistryName() + "@" + (meta == GTValues.W ? "*" : meta);
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.getItem().equals(this.item) && (this.isWildcard() || stack.getMetadata() == this.meta);
    }
}
