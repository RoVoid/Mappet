package mchorse.mappet.network.server.events;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.common.events.PacketTriggeredHotkeys;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerHotkeys extends ServerMessageHandler<PacketTriggeredHotkeys>
{
    @Override
    public void run(EntityPlayerMP player, PacketTriggeredHotkeys message)
    {
        Mappet.settings.hotkeys.execute(player, message.hotkeys);
    }
}