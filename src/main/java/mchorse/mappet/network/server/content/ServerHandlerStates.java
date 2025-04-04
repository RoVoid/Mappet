package mchorse.mappet.network.server.content;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.States;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.network.common.content.PacketStates;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class ServerHandlerStates extends ServerMessageHandler<PacketStates> {
    public static States getStates(MinecraftServer server, String target) {
        if (target.equals("~")) {
            return Mappet.states;
        } else {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(target);
            Character character = Character.get(player);
            if (character != null) return character.getStates();
        }
        return null;
    }

    @Override
    public void run(EntityPlayerMP player, PacketStates message) {
        if (!OpHelper.isPlayerOp(player)) {
            return;
        }

        States states = getStates(player.world.getMinecraftServer(), message.target);

        if (states != null) {
            NBTTagCompound nbt = states.serializeNBT();
            for (String key : message.states.getKeySet()) {
                if (!message.changes.contains(key) && states.values.containsKey(key)) message.states.setTag(key, nbt.getTag(key));
            }
            states.deserializeNBT(message.states);
        }
    }
}