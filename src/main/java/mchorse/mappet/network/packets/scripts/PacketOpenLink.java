package mchorse.mappet.network.packets.scripts;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketOpenLink implements IMessage {
    private String url;

    public PacketOpenLink() {}

    public PacketOpenLink(String url) {
        this.url = url;
    }

    public String getLink() {
        return url;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        url = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, url);
    }
}
