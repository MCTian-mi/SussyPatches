package dev.tianmi.sussypatches.common.helper;

import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.RegistryDefaulted;

import org.jetbrains.annotations.ApiStatus;

import gregtech.api.unification.stack.ItemAndMetadata;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import mcp.MethodsReturnNonnullByDefault;

@ApiStatus.AvailableSince("1.7.0")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DimDisplayRegistry extends RegistryDefaulted<Integer, ItemAndMetadata> {

    private static final DimDisplayRegistry INSTANCE = new DimDisplayRegistry();

    private DimDisplayRegistry() {
        super(new ItemAndMetadata(ItemStack.EMPTY));
    }

    @Override
    protected Map<Integer, ItemAndMetadata> createUnderlyingMap() {
        return new Int2ObjectArrayMap<>();
    }

    public static ItemStack getDisplayItem(int dimId) {
        return INSTANCE.getObject(dimId).toItemStack();
    }

    public static void setDisplayItem(int dimId, ItemStack itemStack) {
        INSTANCE.putObject(dimId, new ItemAndMetadata(itemStack));
    }
}
