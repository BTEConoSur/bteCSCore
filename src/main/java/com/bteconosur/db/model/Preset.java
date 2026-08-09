package com.bteconosur.db.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Preset.PresetId;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.world.block.BaseBlock;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "preset")
/**
 * Entidad de presets por jugador.
 */
public class Preset {

    @EmbeddedId
    private PresetId id;

    @Column(name = "blocks", length = 1000, nullable = false)
    private String blocks;

    @ManyToOne
    @MapsId("uuid")
    @JoinColumn(name = "uuid_player")
    private Player player;

    public Preset() {
    }

    public Preset(UUID id, String nombre, String blocks, Player player) {
        this.id = new PresetId(id, nombre);
        this.blocks = blocks;
        this.player = player;
    }

    public PresetId getId() {
        return id;
    }

    public void setId(PresetId id) {
        this.id = id;
    }

    public String getBlocks() {
        return blocks;
    }

    public void setBlocks(String blocks) {
        this.blocks = blocks;
    }

    public Map<BlockData, Integer> getBlocksMap() {
        return parseBlocks(blocks);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setBlocksMap(Map<BlockData, Integer> blocksMap) {
        this.blocks = blocksMap.entrySet().stream()
            .map(e -> e.getValue() + "%" + e.getKey().getAsString(true).replace("minecraft:", ""))
            .collect(Collectors.joining(","));
    }

    public static Map<BlockData, Integer> parseBlocks(String input) {
        return parseBlocks(input, false);
    }

    public static Map<BlockData, Integer> parseBlocks(String input, boolean strict) throws IllegalArgumentException {
        Map<BlockData, Integer> map = new LinkedHashMap<>();

        if (input == null || input.isBlank()) {
            return map;
        }

        String[] entries = input.split(",");
        for (String e : entries) {
            try {
            
                e = e.trim();
                int percentage;
                String blockString;
                if (e.contains("%")) {
                    String[] split = e.split("%", 2);

                    percentage = Integer.parseInt(split[0].trim());
                    if (percentage > 100) percentage = 100;
                    if (percentage < 0) percentage = 0;
                    blockString = split[1].trim();

                } else {
                    percentage = 100;
                    blockString = e;
                }

                //BlockData bd = Bukkit.createBlockData(blockString);
                ParserContext context = new ParserContext();
                BaseBlock weBlock = WorldEdit.getInstance().getBlockFactory().parseFromInput(blockString, context);
                BlockData bd = BukkitAdapter.adapt(weBlock);
                map.put(bd, percentage);

            } catch (Exception ex) {
                if (strict) {
                    throw new IllegalArgumentException(e);
                } else {
                    ConsoleLogger.error("Error al parsear bloques.", ex);
                }
            }
        }

        return map;
    }

    @Embeddable
    /**
     * Clave compuesta de un preset (jugador + nombre).|
     */
    public static class PresetId implements Serializable {
        
        @Column(name = "uuid_player", columnDefinition = "CHAR(36)", nullable = false)
        @JdbcTypeCode(SqlTypes.CHAR)
        private UUID uuid;

        @Column(name = "nombre", length = 30, nullable = false)
        private String nombre;

        public PresetId() {
        }

        public PresetId(UUID uuid, String nombre) {
            this.uuid = uuid;
            this.nombre = nombre;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PresetId presetId = (PresetId) o;
            return uuid.equals(presetId.uuid) && nombre.equals(presetId.nombre);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, nombre);
        }
    }
}