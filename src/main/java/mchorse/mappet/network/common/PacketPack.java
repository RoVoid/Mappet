package mchorse.mappet.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketPack implements IMessage {
    byte[] pack;

    public PacketPack() {
    }

    public PacketPack(byte[] pack) {
        this.pack = pack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        pack = new byte[length];
        buf.readBytes(pack);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pack.length);
        buf.writeBytes(pack);
    }

    public byte[] getPackData() {
        return pack;
    }
}
