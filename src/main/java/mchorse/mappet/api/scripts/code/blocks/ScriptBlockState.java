package mchorse.mappet.api.scripts.code.blocks;

import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.code.world.ScriptWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ScriptBlockState {
    public static ScriptBlockState AIR = new ScriptBlockState(Blocks.AIR.getDefaultState());
    public static BlockPos.MutableBlockPos BLOCK_POS = new BlockPos.MutableBlockPos();

    private final IBlockState state;

    private ScriptBlockState(IBlockState state) {
        this.state = state;
    }

    public static ScriptBlockState create(IBlockState state) {
        return state == null || state == Blocks.AIR.getDefaultState() ? AIR : new ScriptBlockState(state);
    }

    @Deprecated
    public IBlockState getMinecraftBlockState() {
        return state;
    }

    public IBlockState asMinecraft() {
        return state;
    }

    @Deprecated
    public String getBlockId() {
        ResourceLocation rl = state.getBlock().getRegistryName();
        return rl == null ? "" : rl.toString();
    }

    public String getId() {
        ResourceLocation rl = state.getBlock().getRegistryName();
        return rl == null ? "" : rl.toString();
    }

    public int getMeta() {
        return state.getBlock().getMetaFromState(state);
    }

    public boolean isSame(ScriptBlockState otherState) {
        ScriptBlockState _otherState = (ScriptBlockState) otherState;
        return state.getBlock() == _otherState.state.getBlock() && getMeta() == _otherState.getMeta();
    }

    public boolean isSameBlock(ScriptBlockState otherState) {
        return state.getBlock() == ((ScriptBlockState) otherState).state.getBlock();
    }

    public boolean isOpaque() {
        return state.isOpaqueCube();
    }

    public boolean hasCollision(ScriptWorld world, int x, int y, int z) {
        return state.getCollisionBoundingBox(world.asMinecraft(), BLOCK_POS.setPos(x, y, z)) != null;
    }

    public boolean hasCollision(ScriptWorld world, ScriptVector vector) {
        return state.getCollisionBoundingBox(world.asMinecraft(), BLOCK_POS.setPos(vector.toBlockPos())) != null;
    }

    public boolean isAir() {
        return state.getBlock() == Blocks.AIR;
    }
}