package mchorse.mappet.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketPlayerPerspective implements IMessage {
    int perspective = -1;
    boolean locked = false;

    public PacketPlayerPerspective() {
    }

    public PacketPlayerPerspective(int perspective) {
        this(perspective, false);
    }

    public PacketPlayerPerspective(int perspective, boolean locked) {
        this.perspective = perspective;
        this.locked = locked;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        perspective = buf.readInt();
        locked = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(perspective);
        buf.writeBoolean(locked);
    }

    public int getPerspective() {
        return perspective;
    }

    public boolean locked() {
        return locked;
    }
}
