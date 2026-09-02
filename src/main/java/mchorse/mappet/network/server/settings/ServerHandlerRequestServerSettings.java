package mchorse.mappet.network.server.settings;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.settings.PacketRequestServerSettings;
import mchorse.mappet.network.packets.settings.PacketServerSettings;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerRequestServerSettings extends ServerMessageHandler<PacketRequestServerSettings>
{
    @Override
    public void run(EntityPlayerMP player, PacketRequestServerSettings message)
    {
        if (!OpHelper.isPlayerOp(player))
        {
            return;
        }

        Dispatcher.sendTo(new PacketServerSettings(Mappet.settings.serializeNBT()), player);
    }
}