package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.utils.content.ContentTypes;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class PacketContentBase implements IMessage {
    public IContentTypeBase type;
    public String path;
    public int requestId = -1; // delete

    public PacketContentBase() {}

    public PacketContentBase(IContentTypeBase type) {
        this(type, "");
    }

    public PacketContentBase(IContentTypeBase type, String path) {
        this.type = type;
        this.path = path;
    }

    public PacketContentBase(IContentTypeBase type, int requestId) {
        this(type, "");
        this.requestId = requestId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = ContentTypes.get(ByteBufUtils.readUTF8String(buf));
        path = ByteBufUtils.readUTF8String(buf);
        requestId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, type.name());
        ByteBufUtils.writeUTF8String(buf, path);
        buf.writeInt(requestId);
    }
}