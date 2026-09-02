package mchorse.mappet.network.server.scripts;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.triggers.Trigger;
import mchorse.mappet.network.packets.scripts.PacketClick;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerClick extends ServerMessageHandler<PacketClick> {
    @Override
    public void run(EntityPlayerMP player, PacketClick message) {
        if (message.button == 0 && !Trigger.shouldSkip(Mappet.settings.playerLeftClick)) {
            Mappet.settings.playerLeftClick.trigger(player);
        }
        else if (message.button == 1 && !Trigger.shouldSkip(Mappet.settings.playerRightClick)) {
            Mappet.settings.playerRightClick.trigger(player);
        }
    }
}