package mchorse.mappet.network.packets.blocks;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.conditions.Condition;
import mchorse.mappet.blocks.tile.TileEmitter;
import mchorse.mclib.utils.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketEditEmitter implements IMessage {
    public BlockPos pos;
    public NBTTagCompound condition;
    public float radius;
    public int update;
    public boolean resets;

    public PacketEditEmitter() {}

    public PacketEditEmitter(TileEmitter tile) {
        this(tile.getPos(), tile.getCondition().serializeNBT(), tile.getRadius(), tile.getUpdate(), tile.isReset());
    }

    public PacketEditEmitter(BlockPos pos, NBTTagCompound condition, float radius, int update, boolean resets) {
        this.pos = pos;
        this.condition = condition;
        this.radius = radius;
        this.update = update;
        this.resets = resets;
    }

    public Condition createChecker() {
        Condition condition = new Condition();
        condition.deserializeNBT(this.condition);
        return condition;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        condition = NBTUtils.readInfiniteTag(buf);
        radius = buf.readFloat();
        update = buf.readInt();
        resets = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeTag(buf, condition);
        buf.writeFloat(radius);
        buf.writeInt(update);
        buf.writeBoolean(resets);
    }
}