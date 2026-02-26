package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class PacketContentBase implements IMessage
{
    public IContentTypeBase type;
    public int requestId = -1;

    public PacketContentBase()
    {}

    public PacketContentBase(IContentTypeBase type)
    {
        this.type = type;
    }

    public PacketContentBase(IContentTypeBase type, int requestId)
    {
        this.type = type;
        this.requestId = requestId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.type = ContentTypes.get(ByteBufUtils.readUTF8String(buf));
        this.requestId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, this.type.name());
        buf.writeInt(this.requestId);
    }
}