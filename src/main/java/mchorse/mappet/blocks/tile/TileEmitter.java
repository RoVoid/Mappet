package mchorse.mappet.blocks.tile;

import mchorse.mappet.api.conditions.Condition;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.blocks.BlockEmitter;
import mchorse.mappet.network.packets.blocks.PacketEditEmitter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEmitter extends TileEntity implements ITickable {
    private final Condition condition = new Condition();
    private float radius;
    private int update = 5;
    private boolean resets;

    private int tick = 0;

    public TileEmitter() {}

    public Condition getCondition() {
        return condition;
    }

    public float getRadius() {
        return radius;
    }

    public int getUpdate() {
        return update;
    }

    public boolean isReset() {
        return resets;
    }

    public void applyPacket(PacketEditEmitter message) {
        condition.deserializeNBT(message.condition);
        radius = message.radius;
        update = Math.max(message.update, 1);
        resets = message.resets;
        updateCondition();
        markDirty();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public void update() {
        if (world.isRemote || condition.isEmpty()) return;
        if (tick++ % update == 0) updateCondition();
    }

    private void updateCondition() {
        boolean result = false;
        if (radius > 0) {
            BlockPos pos = getPos();
            for (EntityPlayer player : world.playerEntities)
                if (player.getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= radius) {
                    if (condition.execute(new DataContext(player))) {
                        result = true;
                        break;
                    }
                }
            if (result) {
                updateState(true);
                return;
            }
        }

        /* Don't judge me, I only have one brain cell @TorayLife */
        // ._ .

        if (condition.execute(new DataContext(world, getPos()))) updateState(true);
        else if (resets) updateState(false);
    }

    private void updateState(boolean result) {
        IBlockState state = world.getBlockState(pos);
        if (state.getValue(BlockEmitter.POWERED) != result) world.setBlockState(pos, state.withProperty(BlockEmitter.POWERED, result));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("Condition", condition.serializeNBT());
        if (radius > 0) tag.setFloat("Radius", radius);
        if (update > 0) tag.setInteger("Update", update);
        if (resets) tag.setBoolean("Resets", true);
        return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Condition")) condition.deserializeNBT(tag.getCompoundTag("Condition"));
        if (tag.hasKey("Radius")) radius = tag.getFloat("Radius");
        if (tag.hasKey("Update")) update = tag.getInteger("Update");
        if (tag.hasKey("Resets")) resets = tag.getBoolean("Resets");
    }
}