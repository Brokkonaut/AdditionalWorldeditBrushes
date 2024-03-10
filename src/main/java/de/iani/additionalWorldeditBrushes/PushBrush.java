package de.iani.additionalWorldeditBrushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PushBrush implements Brush {

    private AdditionalWorldeditBrushes plugin;
    private UUID playerId;

    public PushBrush(AdditionalWorldeditBrushes plugin, UUID playerId) {
        this.plugin = plugin;
        this.playerId = playerId;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        int minx = (int) (position.x() - size);
        int maxx = (int) (position.x() + size);
        int miny = (int) (position.y() - size);
        int maxy = (int) (position.y() + size);
        int minz = (int) (position.z() - size);
        int maxz = (int) (position.z() + size);

        double sizesq = size * size;

        // plugin.getLogger().info("Start brush");

        Player builder = plugin.getServer().getPlayer(playerId);
        if (builder == null) {
            return;
        }
        // builder.sendMessage("Start");

        Location player = builder.getLocation();

        double dx = player.getX() - position.x();
        double dy = player.getY() - position.y();
        double dz = player.getZ() - position.z();

        int mx = 0;
        int my = 0;
        int mz = 0;

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (Math.abs(dx) >= Math.abs(dz)) {
                // x
                mx = dx > 0 ? 1 : -1;
            } else {
                // z
                mz = dz > 0 ? 1 : -1;
            }
        } else {
            if (Math.abs(dy) >= Math.abs(dz)) {
                // y
                my = dy > 0 ? 1 : -1;
            } else {
                // z
                mz = dz > 0 ? 1 : -1;
            }
        }

        ArrayList<PostionedBlock> blocksToSet = new ArrayList<>();

        for (int x = minx; x <= maxx; x++) {
            for (int y = miny; y <= maxy; y++) {
                for (int z = minz; z <= maxz; z++) {
                    if (lengthSq(x - position.x(), y - position.y(), z - position.z()) < sizesq) {
                        BlockVector3 pos = BlockVector3.at(x, y, z);
                        BlockState blockHere = editSession.getBlock(pos);
                        // builder.sendMessage("A: " + pos + ": " + blockHere);
                        if (blockHere.getBlockType() != BlockTypes.AIR) {
                            BlockVector3 pos2 = pos.add(mx, my, mz);
                            BlockState blockThere = editSession.getBlock(pos2);
                            // builder.sendMessage("B: " + pos + " -- " + pos2 + ": " + blockThere);
                            if (blockThere.getBlockType() == BlockTypes.AIR) {
                                blocksToSet.add(new PostionedBlock(pos, blockThere.toBaseBlock()));
                                // builder.sendMessage("C: " + pos + ": " + blockThere);
                            }
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
