package dev.tianmi.sussypatches.common.helper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import dev.tianmi.sussypatches.api.util.ItemAndMeta;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.RegistryDefaulted;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
@ApiStatus.AvailableSince("1.7.0")
public class DimDisplayRegistry extends RegistryDefaulted<Integer, ItemAndMeta> {

    private static final DimDisplayRegistry INSTANCE = new DimDisplayRegistry();

    private DimDisplayRegistry() {
        super(ItemAndMeta.EMPTY);
    }

    @Nullable
    public static Integer getDimension(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return INSTANCE.inverse().get(new ItemAndMeta(stack));
    }

    public static ItemStack getDisplayItem(int dimId) {
        return INSTANCE.getObject(dimId).asStack();
    }

    public static void setDisplayItem(int dimId, ItemStack itemStack) {
        INSTANCE.putObject(dimId, new ItemAndMeta(itemStack));
    }

    @Override
    protected Map<Integer, ItemAndMeta> createUnderlyingMap() {
        return HashBiMap.create();
    }

    private Map<ItemAndMeta, Integer> inverse() {
        return ((BiMap<Integer, ItemAndMeta>) registryObjects).inverse();
    }
}
