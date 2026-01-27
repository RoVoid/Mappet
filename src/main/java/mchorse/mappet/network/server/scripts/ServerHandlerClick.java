package mchorse.mappet.network.server.scripts;

import mchorse.mappet.CommonProxy;
import mchorse.mappet.Mappet;
import mchorse.mappet.network.packets.scripts.PacketClick;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerClick extends ServerMessageHandler<PacketClick> {
    @Override
    public void run(EntityPlayerMP player, PacketClick message) {
        if (message.button == 0 && !CommonProxy.triggerEventHandler.shouldSkipTrigger(Mappet.settings.playerLeftClick)) {
            Mappet.settings.playerLeftClick.trigger(player);
        }
        else if (message.button == 1 && !CommonProxy.triggerEventHandler.shouldSkipTrigger(Mappet.settings.playerRightClick)) {
            Mappet.settings.playerRightClick.trigger(player);
        }
    }
}