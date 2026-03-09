package com.bteconosur.db.util;

/**
 * Clave inmutable que identifica un chunk por sus coordenadas X y Z.
 *
 * @param x coordenada X del chunk.
 * @param z coordenada Z del chunk.
 */
public record ChunkKey(int x, int z) {

    /**
     * Crea una clave de chunk a partir de coordenadas de chunk.
     *
     * @param x coordenada X del chunk.
     * @param z coordenada Z del chunk.
     * @return clave de chunk creada.
     */
    public static ChunkKey of(int x, int z) {
        return new ChunkKey(x, z);
    }

    /**
     * Convierte coordenadas de bloque a su chunk correspondiente.
     *
     * @param blockX coordenada X de bloque.
     * @param blockZ coordenada Z de bloque.
     * @return clave del chunk que contiene ese bloque.
     */
    public static ChunkKey fromBlock(int blockX, int blockZ) {
        return new ChunkKey(Math.floorDiv(blockX, 16), Math.floorDiv(blockZ, 16));
    }

}
