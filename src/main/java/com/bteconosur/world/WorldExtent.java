package com.bteconosur.world;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import com.bteconosur.core.BTEConoSur;
import com.bteconosur.core.util.ConsoleLogger;
import com.bteconosur.db.model.Player;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class WorldExtent extends AbstractDelegateExtent {
    
    private final WorldManager worldManager;
    private final Player player;
    private final World world;
    private final ConsoleLogger logger = BTEConoSur.getConsoleLogger();

    public WorldExtent(Extent extent, WorldManager worldManager, Player player, World world) {
        super(extent);
        this.worldManager = worldManager;
        this.player = player;
        this.world = world;
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        if (!worldManager.canBuild(new Location(world, position.getBlockX(), position.getBlockY(), position.getBlockZ()), player)) return false;
        return super.setBiome(position, biome);
    }

    @Override
    public boolean setBiome(int x, int y, int z, BiomeType biome) {
        if (!worldManager.canBuild(new Location(world, x, y, z), player)) return false;
        return super.setBiome(x, y, z, biome);
    }

    @Override
    public boolean setBlock(BlockVector3 location, BlockStateHolder block) throws WorldEditException
    {
        if (!worldManager.canBuild(new Location(world, location.getBlockX(), location.getBlockY(), location.getBlockZ()), player)) {
            logger.debug("[WorldExtent] setBlock denegado para " + location);
            return false;
        }
        return super.setBlock(location, block);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockStateHolder block) throws WorldEditException
    {
        return setBlock(BlockVector3.at(x, y, z), block);
    }

    @Override
    public int setBlocks(final @NotNull Set<BlockVector3> vset, final Pattern pattern) {
        int count = 0;
        for (BlockVector3 position : vset) {
            try {
                if (setBlock(position, pattern.applyBlock(position))) {
                    count++;
                }
            } catch (WorldEditException e) {
            }
        }
        return count;
    }

    @Override
    public int setBlocks(@NotNull Region region, Pattern pattern) throws MaxChangedBlocksException
    {
        logger.debug("[WorldExtent] setBlocks(Region, Pattern) llamado para región");
        int count = 0;
        for (BlockVector3 position : region) {
            if (setBlock(position, pattern.applyBlock(position))) count += 1;
        }
        logger.debug("[WorldExtent] setBlocks(Region, Pattern) estableció " + count + " bloques");
        return count;
    }

    @Override
    public <B extends BlockStateHolder<B>> int setBlocks(final Region region, final B block) throws MaxChangedBlocksException {
        logger.debug("[WorldExtent] setBlocks(Region, Block) llamado para región");
        int count = 0;
        for (BlockVector3 position : region) {
            if (setBlock(position, block)) count += 1;
        }
        logger.debug("[WorldExtent] setBlocks(Region, Block) estableció " + count + " bloques");
        return count;
    }

    @Override
    public int replaceBlocks(@NotNull Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException
    {
        logger.debug("[WorldExtent] replaceBlocks(Region, Mask, Pattern) llamado");
        int count = 0;
        for (BlockVector3 position : region) {
            if (mask.test(position) && setBlock(position, pattern.applyBlock(position))) count++;
        }
        logger.debug("[WorldExtent] replaceBlocks(Region, Mask, Pattern) reemplazó " + count + " bloques");
        return count;
    }

    @Override
    public <B extends BlockStateHolder<B>> int replaceBlocks(final @NotNull Region region, final Set<BaseBlock> filter, final B replacement) throws MaxChangedBlocksException
    {
        logger.debug("[WorldExtent] replaceBlocks(Region, Set, Block) llamado");
        int count = 0;
        for (BlockVector3 position : region) {
            if (filter.contains(getFullBlock(position)) && setBlock(position, replacement)) {
                count++;
            }
        }
        logger.debug("[WorldExtent] replaceBlocks(Region, Set, Block) reemplazó " + count + " bloques");
        return count;
    }

    @Override
    public int replaceBlocks(final @NotNull Region region, final Set<BaseBlock> filter, final Pattern pattern) throws MaxChangedBlocksException
    {
        logger.debug("[WorldExtent] replaceBlocks(Region, Set, Pattern) llamado");
        int count = 0;
        for (BlockVector3 position : region) {
            if (filter.contains(getFullBlock(position)) && setBlock(position, pattern.applyBlock(position))) {
                count++;
            }
        }
        logger.debug("[WorldExtent] replaceBlocks(Region, Set, Pattern) reemplazó " + count + " bloques");
        return count;
    }
}
