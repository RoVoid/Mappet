package mchorse.mappet.network.client;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.common.PacketBlackAndWhiteShader;
import mchorse.mappet.network.common.PacketLockPerspective;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class ClientHandlerLockPerspective extends ClientMessageHandler<PacketLockPerspective> {
    private static int lockedPerspective = 0;

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
