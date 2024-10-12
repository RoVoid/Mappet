package mchorse.mappet.network.common;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketLockPerspective implements IMessage {
    int lockedPerspective = -1;

    public PacketLockPerspective() {
    }

    public PacketLockPerspective(int lockedPerspective) {
        this.lockedPerspective = lockedPerspective;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        lockedPerspective = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(lockedPerspective);
    }

    public int getLockedPerspective() {
        return lockedPerspective;
    }
}
