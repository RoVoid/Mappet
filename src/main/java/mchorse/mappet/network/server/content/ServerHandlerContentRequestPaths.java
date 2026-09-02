package mchorse.mappet.network.server.content;

import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentPaths;
import mchorse.mappet.network.packets.content.PacketContentRequestPaths;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Set;

public class ServerHandlerContentRequestPaths extends ServerMessageHandler<PacketContentRequestPaths> {
    @Override
    public void run(EntityPlayerMP player, PacketContentRequestPaths message) {
        if (!PlayerUtils.isOperator(player)) return;
        Set<String> paths = message.type.manager().getPaths();
        Dispatcher.sendTo(new PacketContentPaths(message.type, paths, message.requestId), player);
    }
}