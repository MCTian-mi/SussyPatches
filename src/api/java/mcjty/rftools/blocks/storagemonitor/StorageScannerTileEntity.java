package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.container.InventoryHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.util.Set;
import java.util.stream.Stream;

/// Adapted and minimized from
/// [ReFinedTools](https://github.com/MCTian-mi/ReFinedTools/blob/main/src/main/java/mcjty/rftools/blocks/storagemonitor/StorageScannerTileEntity.java)
@SuppressWarnings("ResultOfMethodCallIgnored")
public class StorageScannerTileEntity {

    public Stream<BlockPos> findInventories() {
        InventoryHelper.isInventory(null);
        return null;
    }

    private void inventoryAddNew(Set<BlockPos> oldAdded, Set<IItemHandler> seenItemHandlers, BlockPos p) {
        InventoryHelper.isInventory(null);
    }
}
