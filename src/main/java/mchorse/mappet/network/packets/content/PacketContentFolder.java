package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketContentFolder extends PacketContentBase {
    // default: create
    public String rename;
    public Boolean delete = false;

    public PacketContentFolder() {
        super();
    }

    public PacketContentFolder(IContentTypeBase type, String path) {
        super(type, path);
    }

    public PacketContentFolder rename(String rename) { // change to newPath
        this.rename = rename;
        delete = false;
        return this;
    }

    public PacketContentFolder delete() {
        rename = "";
        delete = true;
        return this;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        delete = buf.readBoolean();
        if (delete) return;

        if (buf.readBoolean()) rename = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        buf.writeBoolean(delete);
        if (delete) return;

        buf.writeBoolean(rename != null);
        if (rename != null) ByteBufUtils.writeUTF8String(buf, rename);
    }
}
