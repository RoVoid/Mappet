package mchorse.mappet.network.client;

import mchorse.mappet.network.common.PacketCamera;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;

public class ClientHandlerCamera extends ClientMessageHandler<PacketCamera> {
    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketCamera message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;

        Entity target = null;
        for (Entity e : mc.world.loadedEntityList) {
            if (e.getUniqueID().equals(message.getUuid())) {
                target = e;
                break;
            }
        }
        mc.setRenderViewEntity(target == null ? mc.player : target);
    }
}
