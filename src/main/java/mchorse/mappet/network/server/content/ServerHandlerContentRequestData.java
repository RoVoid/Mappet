package mchorse.mappet.network.server.content;

import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentData;
import mchorse.mappet.network.packets.content.PacketContentRequestData;
import mchorse.mappet.utils.CurrentSession;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class ServerHandlerContentRequestData extends ServerMessageHandler<PacketContentRequestData> {

    @Override
    public void run(EntityPlayerMP player, PacketContentRequestData message) {
        if (!PlayerUtils.isOperator(player) || player.getServer() == null) return;

        ICharacter character = Character.get(player);
        if (character == null) return;
        CurrentSession session = character.getCurrentSession();

        String editorName = null;
        if (session.isEditing(message.type, message.path)) editorName = player.getName();
        else for (EntityPlayerMP other : player.getServer().getPlayerList().getPlayers()) {
            if (other == player) continue;

            ICharacter otherCharacter = Character.get(other);
            if (otherCharacter != null && otherCharacter.getCurrentSession().isEditing(message.type, message.path)) {
                editorName = other.getName();
                break;
            }
        }

        NBTTagCompound tag = message.type.manager().load(message.path).serializeNBT();
        PacketContentData packet = new PacketContentData(message.type, message.path, tag);
        packet.editorName = editorName;

        Dispatcher.sendTo(packet, player);
        if (editorName == null) session.hold(message.type, message.path);
        else session.observe(message.type, message.path);
    }
}
