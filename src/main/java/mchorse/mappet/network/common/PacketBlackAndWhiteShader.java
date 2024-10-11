package mchorse.mappet.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlackAndWhiteShader implements IMessage {
    boolean enable = false;

    public PacketBlackAndWhiteShader() {
    }

    public PacketBlackAndWhiteShader(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        enable = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(enable);
    }

    public boolean isEnable() {
        return enable;
    }
}
