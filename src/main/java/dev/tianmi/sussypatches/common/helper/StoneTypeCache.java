package dev.tianmi.sussypatches.common.helper;

import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.unification.ore.StoneType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.block.state.IBlockState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiPredicate;

@NullMarked
public final class StoneTypeCache {

    private static final StoneType NULL = initNullStoneType();
    private static final Map<IBlockState, StoneType> CACHE = new Reference2ObjectOpenHashMap<>(1024);

    @SneakyThrows
    private static StoneType initNullStoneType() {
        return (StoneType) SusUtil.UNSAFE.allocateInstance(StoneType.class);
    }

    public static @Nullable StoneType get(IBlockState state, BiPredicate<StoneType, IBlockState> filter) {
        val result = CACHE.computeIfAbsent(state, it -> {
            for (val type : StoneType.STONE_TYPE_REGISTRY) {
                if (filter.test(type, it)) {
                    return type;
                }
            }
            return NULL;
        });
        return result == NULL ? null : result;
    }
}
