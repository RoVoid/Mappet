package mchorse.mappet.network.client;

import mchorse.mappet.network.common.PacketLockPerspective;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;

public class ClientHandlerLockPerspective extends ClientMessageHandler<PacketLockPerspective> {
    private static int lockedPerspective = -1;

    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketLockPerspective message) {
        lockedPerspective = Math.max(Math.min(message.getLockedPerspective(), 2), -1);
    }

    public static void setLockedPerspective(int perspective) {
        lockedPerspective = Math.max(Math.min(perspective, 2), -1);
    }

    public static int getLockedPerspective() {
        return lockedPerspective;
    }
}
