package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PacketContentNames extends PacketContentBase
{
    public Set<String> names = new HashSet<>();

    public PacketContentNames()
    {
        super();
    }

    public PacketContentNames(IContentTypeBase type, List<String> names)
    {
        super(type);

        this.names.addAll(names);
    }

    public PacketContentNames(IContentTypeBase type, List<String> names, int requestId)
    {
        super(type, requestId);

        this.names.addAll(names);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);

        for (int i = 0, c = buf.readInt(); i < c; i++)
        {
            this.names.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeInt(this.names.size());

        for (String name : this.names)
        {
            ByteBufUtils.writeUTF8String(buf, name);
        }
    }
}