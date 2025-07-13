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

    public ScriptBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    @Override
    public boolean isColliding(ScriptBox box) {
        return minX < box.maxX && maxX > minX && minY < maxY && maxY > box.minY && minZ < box.maxZ && maxZ > box.minZ;
    }

    @Override
    public void offset(double x, double y, double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;

        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
    }

    @Override
    public boolean contains(ScriptVector vector) {
        return this.contains(vector.x, vector.y, vector.z);
    }

    @Override
    public List<ScriptVector> getBlocksPositions(ScriptWorld world, ScriptBlockState state) {
        World minecraftWorldworld = world.getMinecraftWorld();
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
