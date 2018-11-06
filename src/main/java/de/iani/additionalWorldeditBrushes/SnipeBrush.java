package de.iani.additionalWorldeditBrushes;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

public class SnipeBrush implements Brush {

    private AdditionalWorldeditBrushes plugin;
    private UUID playerId;
    // private BrushTool brushTool;

    public SnipeBrush(AdditionalWorldeditBrushes plugin, UUID playerId, BrushTool brushTool) {
        this.plugin = plugin;
        this.playerId = playerId;
        // this.brushTool = brushTool;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        Player builder = plugin.getServer().getPlayer(playerId);
        if (builder == null) {
            return;
        }
        List<Block> blocks = builder.getLineOfSight((Set<Material>) null, 120);
        if (blocks.size() > 1) {
            Block lastAir = blocks.get(blocks.size() - 2);
            if (lastAir.getType() == Material.AIR) {
                BlockVector3 position2 = BlockVector3.at(lastAir.getX(), lastAir.getY(), lastAir.getZ());
                editSession.setBlock(position2, pattern.apply(position2));
            }
        }
        // BukkitPlayer wePlayer = plugin.getWorldEdit().wrapPlayer(builder);
        // WorldVectorFace target = wePlayer.getBlockTraceFace(brushTool.getRange(), true);
        // Vector position2 = position;
        // if (target != null) {
        // position2 = target.getFaceVector();
        // }
        // BaseBlock blockType = pattern.apply(position2);
        // editSession.setBlock(position2, blockType);
    }
}
