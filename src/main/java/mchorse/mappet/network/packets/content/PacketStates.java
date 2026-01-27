package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mclib.utils.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class PacketStates implements IMessage
{
    public String target;
    public NBTTagCompound states;
    public List<String> changes;

    public PacketStates()
    {}

    public PacketStates(String target, NBTTagCompound states)
    {
        this(target, states, new ArrayList<>());
    }

    public PacketStates(String target, NBTTagCompound states, List<String> changes) {
        this.target = target;
        this.states = states;
        this.changes = changes;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.target = ByteBufUtils.readUTF8String(buf);
        this.states = NBTUtils.readInfiniteTag(buf);
        this.changes = new ArrayList<>();
        int size = buf.readInt();
        for(int i = 0; i < size; i++) this.changes.add(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.target);
        ByteBufUtils.writeTag(buf, this.states);
        buf.writeInt(this.changes.size());
        for(String str : this.changes) ByteBufUtils.writeUTF8String(buf, str);
    }
}