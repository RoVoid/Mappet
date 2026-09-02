package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import mchorse.mclib.utils.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketContentData extends PacketContentBase {
    public NBTTagCompound data;
    public String editorName;

    public PacketContentData() {
        super();
    }

    public PacketContentData(IContentTypeBase type, String path) {
        super(type, path);
    }

    public PacketContentData(IContentTypeBase type, String path, NBTTagCompound data) {
        super(type, path);
        this.data = data;
    }

    public PacketContentData create() {
        data = new NBTTagCompound();
        return this;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        if (buf.readBoolean()) editorName = ByteBufUtils.readUTF8String(buf);
        if (buf.readBoolean()) data = NBTUtils.readInfiniteTag(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        buf.writeBoolean(editorName != null);
        if (editorName != null) ByteBufUtils.writeUTF8String(buf, this.editorName);

        buf.writeBoolean(data != null);
        if (data != null) ByteBufUtils.writeTag(buf, data);
    }

    public void disallow(){ // for dev
        editorName = ".";
    }
}