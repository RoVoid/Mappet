package mchorse.mappet.network.packets.scripts;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketClipboard implements IMessage {
    public String clipboard = "";

    public PacketClipboard() {
    }

    public PacketClipboard(String clipboard) {
        this.clipboard = clipboard;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.clipboard = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.clipboard);
    }
}
