package postInit

// Throwaway smoke test for the recycling DAG. Copy into run/groovy/postInit/ to run it, then delete it again.
//
// Order matters on purpose: the additive call happens first, so that minecraft:bow becomes an ingredient of
// minecraft:dispenser, and the replacement right after it walks into a node which already has a downstream consumer.

def recycling = mods.sussypatches.recycling

// log.info("SMOKE 1/4 >> deriving the recycling data of minecraft:dispenser from its ingredients")
// recycling.addRecycling(item('minecraft:dispenser'), [item('minecraft:bow'), item('minecraft:cobblestone'), item('minecraft:redstone')])

// log.info("SMOKE 2/4 >> replacing the recycling data of minecraft:bow, one of the ingredients of minecraft:dispenser")
// recycling.registerRecycling(item('minecraft:bow'), [
//         (item('minecraft:stick')): 3,
//         'ingotIron'              : 1,    // a full oreDict name, resolved through ceu
//         'ingot'                  : 99    // a bare prefix name, dropped on purpose
// ], 1)

// log.info("SMOKE 3/4 >> replacing the recycling data of minecraft:hopper")
// recycling.registerRecycling(item('minecraft:hopper'), [(item('minecraft:iron_ingot')): 5], 1)

// log.info("SMOKE 4/4 >> dropping the recycling data of minecraft:furnace")
// recycling.removeRecycling(item('minecraft:furnace'))

// log.info("SMOKE >> all script calls went through")

// recycling.registerRecycling(item('bdsandm:metal_barrel'), [(item('minecraft:hopper')): 2], 1)
