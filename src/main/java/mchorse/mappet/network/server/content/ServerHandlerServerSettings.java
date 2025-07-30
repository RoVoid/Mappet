package mchorse.mappet.network.server.content;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.content.PacketServerSettings;
import mchorse.mappet.network.common.events.PacketSyncHotkeys;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class ServerHandlerServerSettings extends ServerMessageHandler<PacketServerSettings> {
    @Override
    public void run(EntityPlayerMP player, PacketServerSettings message) {
        if (!OpHelper.isPlayerOp(player)) return;

        Mappet.settings.deserializeNBT(message.tag);
        Mappet.settings.save();

        MinecraftServer server = player.getServer();
        if (server != null)
            for (EntityPlayerMP p : server.getPlayerList().getPlayers()) {
                Dispatcher.sendTo(new PacketSyncHotkeys(Mappet.settings), p);
            }
    }
}