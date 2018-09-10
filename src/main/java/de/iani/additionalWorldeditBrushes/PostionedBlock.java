package de.iani.additionalWorldeditBrushes;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.block.BaseBlock;

public final class PostionedBlock {
    private final Vector position;
    private final BaseBlock block;

    public PostionedBlock(Vector position, BaseBlock block) {
        this.position = position;
        this.block = block;
    }

    public Vector getPosition() {
        return position;
    }

    public BaseBlock getBlock() {
        return block;
    }
}