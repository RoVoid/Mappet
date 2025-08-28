package mchorse.mappet.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketScreenshot implements IMessage {
    private String name = "";
    private boolean share = false;
    private byte[] data;

    public PacketScreenshot() {
    }

    public PacketScreenshot(String name, boolean share) {
        this.name = name;
        this.share = share;
    }

    public PacketScreenshot(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
        share = buf.readBoolean();
        data = new byte[buf.readInt()];
        buf.readBytes(data);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeBoolean(share);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    public String getName() {
        return name;
    }

    public boolean needShare() {
        return share;
    }

    public byte[] getData() {
        return data;
    }
}
