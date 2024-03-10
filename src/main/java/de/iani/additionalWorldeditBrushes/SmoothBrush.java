package de.iani.additionalWorldeditBrushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class SmoothBrush implements Brush {

    // private AdditionalWorldeditBrushes plugin;

    private final int bias;

    public SmoothBrush(AdditionalWorldeditBrushes plugin, int bias) {
        // this.plugin = plugin;
        this.bias = bias;
    }

    public boolean isAirlike(BlockType m) {
        return m == BlockTypes.AIR || m == BlockTypes.CAVE_AIR || m == BlockTypes.WATER || m == BlockTypes.LAVA || m == BlockTypes.SHORT_GRASS || !m.getMaterial().isMovementBlocker();
    }

    public boolean isVeryAirlike(BlockType m) {
        return m == BlockTypes.AIR || m == BlockTypes.CAVE_AIR || m == BlockTypes.WATER || m == BlockTypes.LAVA;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        int sizeInt = (int) size;

        int minx = (position.x()) - sizeInt;
        int maxx = (position.x()) + sizeInt;
        int miny = (position.y()) - sizeInt;
        int maxy = (position.y()) + sizeInt;
        int minz = (position.z()) - sizeInt;
        int maxz = (position.z()) + sizeInt;

        double sizesq = size * size;

        BlockState[][][] blocks = new BlockState[sizeInt * 2 + 1][sizeInt * 2 + 1][sizeInt * 2 + 1];
        for (int x = minx; x <= maxx; x++) {
            for (int y = miny; y <= maxy; y++) {
                for (int z = minz; z <= maxz; z++) {
                    BlockVector3 pos = BlockVector3.at(x, y, z);
                    blocks[x - minx][y - miny][z - minz] = editSession.getBlock(pos);
                }
            }
        }

        ArrayList<PostionedBlock> blocksToSet = new ArrayList<>();
        HashMap<BlockType, Integer> blocksNeighbour = new HashMap<>();
        HashMap<BlockType, BlockState> blocksNeighbourData = new HashMap<>();
        for (int x = minx + 1; x <= maxx - 1; x++) {
            for (int y = miny + 1; y <= maxy - 1; y++) {
                for (int z = minz + 1; z <= maxz - 1; z++) {
                    if (lengthSq(x - position.x(), y - position.y(), z - position.z()) < sizesq) {
                        BlockVector3 pos = BlockVector3.at(x, y, z);
                        BlockState blockHere = blocks[x - minx][y - miny][z - minz];

                        BlockVector3[] neighbours = new BlockVector3[26];
                        int nr = 0;
                        for (int x1 = x - 1; x1 <= x + 1; x1++) {
                            for (int y1 = y - 1; y1 <= y + 1; y1++) {
                                for (int z1 = z - 1; z1 <= z + 1; z1++) {
                                    if (x1 != x || y1 != y || z1 != z) {
                                        if (x1 == x || y1 == y || z1 == z) {
                                            neighbours[nr++] = BlockVector3.at(x1, y1, z1);
                                        }
                                    }
                                }
                            }
                        }
                        // neighbours[0] = new Vector(x - 1, y, z);
                        // neighbours[1] = new Vector(x + 1, y, z);
                        // neighbours[2] = new Vector(x, y - 1, z);
                        // neighbours[3] = new Vector(x, y + 1, z);
                        // neighbours[4] = new Vector(x, y, z - 1);
                        // neighbours[5] = new Vector(x, y, z + 1);

                        int airCount = 0;

                        for (BlockVector3 pos2 : neighbours) {
                            if (pos2 != null) {
                                BlockState blockThere = blocks[pos2.x() - minx][pos2.y() - miny][pos2.z() - minz];
                                if (isAirlike(blockThere.getBlockType())) {
                                    airCount++;
                                }
                            }
                        }

                        if (isAirlike(blockHere.getBlockType()) && airCount < 8 + bias) {
                            blocksNeighbour.clear();
                            blocksNeighbourData.clear();
                            for (BlockVector3 pos2 : neighbours) {
                                if (pos2 != null) {
                                    BlockState blockThere = blocks[pos2.x() - minx][pos2.y() - miny][pos2.z() - minz];
                                    BlockType t = blockThere.getBlockType();
                                    if (!isAirlike(t)) {
                                        Integer existing = blocksNeighbour.get(t);
                                        blocksNeighbour.put(t, existing == null ? 1 : existing + 1);
                                        blocksNeighbourData.put(t, blockThere.toImmutableState());
                                    }
                                }
                            }
                            int max = 0;
                            BlockState maxd = null;
                            for (Entry<BlockType, Integer> e : blocksNeighbour.entrySet()) {
                                if (e.getValue() > max) {
                                    max = e.getValue();
                                    maxd = blocksNeighbourData.get(e.getKey());
                                }
                            }
                            blocksToSet.add(new PostionedBlock(pos, maxd.toBaseBlock()));
                        } else if (!isAirlike(blockHere.getBlockType()) && airCount > 10 + bias) {
                            blocksNeighbour.clear();
                            blocksNeighbourData.clear();
                            for (BlockVector3 pos2 : neighbours) {
                                if (pos2 != null) {
                                    BlockState blockThere = blocks[pos2.x() - minx][pos2.y() - miny][pos2.z() - minz];
                                    BlockType t = blockThere.getBlockType();
                                    if (isVeryAirlike(t)) {
                                        Integer existing = blocksNeighbour.get(t);
                                        blocksNeighbour.put(t, existing == null ? 1 : existing + 1);
                                        blocksNeighbourData.put(t, blockThere.toImmutableState());
                                    }
                                }
                            }
                            int max = 0;
                            BlockState maxd = null;
                            for (Entry<BlockType, Integer> e : blocksNeighbour.entrySet()) {
                                if (e.getValue() > max) {
                                    max = e.getValue();
                                    maxd = blocksNeighbourData.get(e.getKey());
                                }
                            }
                            blocksToSet.add(new PostionedBlock(pos, maxd == null ? BlockTypes.AIR.getDefaultState().toBaseBlock() : maxd.toBaseBlock()));
                        }
                    }
                }
            }
        }

        for (PostionedBlock bp : blocksToSet) {
            editSession.setBlock(bp.getPosition(), bp.getBlock());
        }

        // builder.sendMessage("Done");
        // editSession.c

    }

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }
}
