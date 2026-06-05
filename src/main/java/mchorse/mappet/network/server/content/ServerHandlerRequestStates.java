package mchorse.mappet.network.server.content;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketRequestStates;
import mchorse.mappet.network.packets.content.PacketStates;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerRequestStates extends ServerMessageHandler<PacketRequestStates> {
    @Override
    public void run(EntityPlayerMP player, PacketRequestStates message) {
        if (!OpHelper.isPlayerOp(player)) return;

        ScriptStates states = ServerHandlerStates.getStates(player.world.getMinecraftServer(), message.target);
        if (states == null) {
            message.target = "~";
            states = Mappet.states.scripts;
        }
        Dispatcher.sendTo(new PacketStates(message.target, states.serializeNBT()), player);
    }
}