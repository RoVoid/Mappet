package mchorse.mappet.network.client;

import mchorse.mappet.network.common.PacketPlayerPerspective;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class ClientHandlerPlayerPerspective extends ClientMessageHandler<PacketPlayerPerspective> {
    private static int perspective = -1;
    private static boolean locked = false;

    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketPlayerPerspective message) {
        if (message.getPerspective() == -1) perspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;
        else perspective = Math.max(Math.min(message.getPerspective(), 2), 0);
        locked = message.locked();
    }

    public static void setPerspective(int perspective) {
        ClientHandlerPlayerPerspective.perspective = Math.max(Math.min(perspective, 2), -1);
    }

    public static int getPerspective() {
        return perspective;
    }

    public static boolean locked() {
        return locked;
    }
}
