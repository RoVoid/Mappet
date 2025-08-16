package mchorse.mappet.api.scripts.code.data;

import mchorse.mappet.api.scripts.code.blocks.ScriptBlockState;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import mchorse.mappet.api.scripts.user.data.IScriptBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ScriptBox implements IScriptBox {
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public static ScriptBox EMPTY = new ScriptBox(0, 0, 0, 0, 0, 0);

    public ScriptBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public ScriptBox(ScriptVector vec1, ScriptVector vec2) {
        minX = Math.min(vec1.x, vec2.x);
        minY = Math.min(vec1.y, vec2.y);
        minZ = Math.min(vec1.z, vec2.z);
        maxX = Math.max(vec1.x, vec2.x);
        maxY = Math.max(vec1.y, vec2.y);
        maxZ = Math.max(vec1.z, vec2.z);
    }

    @Override
    public boolean isColliding(ScriptBox box) {
        return minX < box.maxX && maxX > minX && minY < maxY && maxY > box.minY && minZ < box.maxZ && maxZ > box.minZ;
    }

    @Override
    public void offset(double x, double y, double z) {
        minX += x;
        minY += y;
        minZ += z;

        maxX += x;
        maxY += y;
        maxZ += z;
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    @Override
    public boolean contains(ScriptVector vector) {
        return contains(vector.x, vector.y, vector.z);
    }

    @Override
    public List<ScriptVector> getBlocksPositions(ScriptWorld world, ScriptBlockState state) {
        World minecraftWorldworld = world.asMinecraft();
        IBlockState blockState = state.asMinecraft();

        List<ScriptVector> blocks = new ArrayList<>();

        for (double x = minX; x <= maxX; x++) {
            for (double y = minY; y <= maxY; y++) {
                for (double z = minZ; z <= maxZ; z++) {
                    if (minecraftWorldworld.getBlockState(new BlockPos(x, y, z)).equals(blockState)) {
                        blocks.add(new ScriptVector(x, y, z));
                    }
                }
            }
        }

        return blocks;
    }

    @Override
    public String toString() {
        return "ScriptBox(" + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ")";
    }
}
