package de.iani.additionalWorldeditBrushes;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

public final class PostionedBlock {
    private final BlockVector3 position;
    private final BaseBlock block;

    public PostionedBlock(BlockVector3 position, BaseBlock block) {
        this.position = position;
        this.block = block;
    }

    public BlockVector3 getPosition() {
        return position;
    }

    public BaseBlock getBlock() {
        return block;
    }
}