package tech.endorsed.signport.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;

public class Anchor {
    public String name;
    public BlockPos pos;

    public Anchor(String name, int x, int y, int z)
    {
        this.name = name;
        pos = new BlockPos(x, y, z);
    }

    public Anchor(String name, BlockPos pos)
    {
        this.name = name;
        this.pos = pos;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int getZ() {
        return pos.getZ();
    }
}
