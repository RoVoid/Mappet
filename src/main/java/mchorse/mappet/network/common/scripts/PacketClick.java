package mchorse.mappet.network.common.scripts;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketClick implements IMessage {
    public int button = 0; // left

    public PacketClick() {
    }

    public PacketClick(int button) {
        this.button = button;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        button = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(button);
    }
}
