package com.bteconosur.db.util;

public record ChunkKey(int x, int z) {

    public static ChunkKey of(int x, int z) {
        return new ChunkKey(x, z);
    }

    public static ChunkKey fromBlock(int blockX, int blockZ) {
        return new ChunkKey(Math.floorDiv(blockX, 16), Math.floorDiv(blockZ, 16));
    }

}
