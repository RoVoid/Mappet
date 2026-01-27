package mchorse.mappet.network.client;

import mchorse.mappet.client.CameraReflect;
import mchorse.mappet.network.packets.PacketCamera;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;

public class ClientHandlerCamera extends ClientMessageHandler<PacketCamera> {
    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketCamera message) {
        CameraReflect.update(message.getTag());
    }
}
