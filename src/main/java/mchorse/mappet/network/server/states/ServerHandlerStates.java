package mchorse.mappet.network.server.states;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.network.packets.states.PacketStates;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class ServerHandlerStates extends ServerMessageHandler<PacketStates> {
    public static ScriptStates getStates(MinecraftServer server, String target) {
        if (target.equals("~")) return Mappet.states.scripts;

        Character character = Character.get(server.getPlayerList().getPlayerByUsername(target));
        return character == null ? null : character.getStates().scripts;
    }

    @Override
    public void run(EntityPlayerMP player, PacketStates message) {
        if (!OpHelper.isPlayerOp(player)) return;

        Mappet.logger.debug(message.target, message.states.toString());
        ScriptStates states = getStates(player.world.getMinecraftServer(), message.target);
        if (states == null) return;

        NBTTagCompound nbt = states.serializeNBT();
        for (String key : message.states.getKeySet()) {
            if (!message.changes.contains(key) && states.has(key)) message.states.setTag(key, nbt.getTag(key));
        }
        states.deserializeNBT(message.states);

        if (message.target.equals("~")) Mappet.states.save();
    }
}