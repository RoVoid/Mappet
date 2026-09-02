package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketContentRename extends PacketContentBase
{
    public String newPath;
    public PacketContentRename()
    {
        super();
    }

    public PacketContentRename(IContentTypeBase type, String path, String newPath)
    {
        super(type, path);
        this.newPath = newPath;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        super.fromBytes(buf);
        if (buf.readBoolean()) newPath = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeBoolean(newPath != null);
        if (newPath != null) ByteBufUtils.writeUTF8String(buf, newPath);
    }
}