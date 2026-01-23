package mchorse.mappet.api.scripts.code.blocks;

import mchorse.mappet.api.scripts.user.blocks.IScriptBlockState;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.user.world.IScriptWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class ScriptBlockState implements IScriptBlockState {
    public static ScriptBlockState AIR = new ScriptBlockState(Blocks.AIR.getDefaultState());
    public static BlockPos.MutableBlockPos BLOCK_POS = new BlockPos.MutableBlockPos();

    private final IBlockState state;

    private ScriptBlockState(IBlockState state) {
        this.state = state;
    }

    public static IScriptBlockState create(IBlockState state) {
        return state == null || state == Blocks.AIR.getDefaultState() ? AIR : new ScriptBlockState(state);
    }

    @Override
    @Deprecated
    public IBlockState getMinecraftBlockState() {
        return state;
    }

    @Override
    public IBlockState asMinecraft() {
        return state;
    }

    @Override
    @Deprecated
    public String getBlockId() {
        ResourceLocation rl = state.getBlock().getRegistryName();
        return rl == null ? "" : rl.toString();
    }

    @Override
    public String getId() {
        ResourceLocation rl = state.getBlock().getRegistryName();
        return rl == null ? "" : rl.toString();
    }

    @Override
    public int getMeta() {
        return state.getBlock().getMetaFromState(state);
    }

    @Override
    public boolean isSame(IScriptBlockState otherState) {
        ScriptBlockState _otherState = (ScriptBlockState) otherState;
        return state.getBlock() == _otherState.state.getBlock() && getMeta() == _otherState.getMeta();
    }

    @Override
    public boolean isSameBlock(IScriptBlockState otherState) {
        return state.getBlock() == ((ScriptBlockState) otherState).state.getBlock();
    }

    @Override
    public boolean isOpaque() {
        return state.isOpaqueCube();
    }

    @Override
    public boolean hasCollision(IScriptWorld world, int x, int y, int z) {
        return state.getCollisionBoundingBox(world.asMinecraft(), BLOCK_POS.setPos(x, y, z)) != null;
    }

    @Override
    public boolean hasCollision(IScriptWorld world, ScriptVector vector) {
        return state.getCollisionBoundingBox(world.asMinecraft(), BLOCK_POS.setPos(vector.toBlockPos())) != null;
    }

    @Override
    public boolean isAir() {
        return state.getBlock() == Blocks.AIR;
    }
}