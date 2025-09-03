package dev.tianmi.sussypatches.api.unification.info;

import gregtech.api.unification.material.info.MaterialIconType;

public interface SusIconTypes {

    // Item & fluid pipes
    MaterialIconType pipeTiny = namely("pipeTiny");
    MaterialIconType pipeSmall = namely("pipeSmall");
    MaterialIconType pipeNormal = namely("pipeNormal");
    MaterialIconType pipeLarge = namely("pipeLarge");
    MaterialIconType pipeHuge = namely("pipeHuge");
    MaterialIconType pipeQuadruple = namely("pipeQuadruple");
    MaterialIconType pipeNonuple = namely("pipeNonuple");
    MaterialIconType pipeSide = namely("pipeSide");

    // Wires & cables
    MaterialIconType wire = namely("wire"); // All wires & cables uses the same base texture
    MaterialIconType insulationSingle = namely("insulationSingle");
    MaterialIconType insulationDouble = namely("insulationDouble");
    MaterialIconType insulationQuadruple = namely("insulationQuadruple");
    MaterialIconType insulationOctal = namely("insulationOctal");
    MaterialIconType insulationHex = namely("insulationHex");
    MaterialIconType insulationSide = namely("insulationSide");

    static MaterialIconType namely(String name) {
        return new MaterialIconType(name);
    }
}
