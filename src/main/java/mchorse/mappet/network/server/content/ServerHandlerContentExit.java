package mchorse.mappet.network.server.content;

import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentData;
import mchorse.mappet.network.packets.content.PacketContentExit;
import mchorse.mappet.utils.CurrentSession;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class ServerHandlerContentExit extends ServerMessageHandler<PacketContentExit> {
    public static void finishEditing(EntityPlayerMP prevEditor) {
        /* Before clearing current session, update the data for players that
         * are browsing this data */
        ICharacter character = Character.get(prevEditor);
        if (character == null) return;

        CurrentSession session = character.getCurrentSession();
        if (session.editingType == null) return;

        boolean first = true;

        if (prevEditor.getServer() == null) return;

        NBTTagCompound data = session.editingType.manager().load(session.editingId).serializeNBT();

        PacketContentData packet = new PacketContentData(session.editingType, session.editingId, data);

        for (EntityPlayerMP player : prevEditor.getServer().getPlayerList().getPlayers()) {
            if (player == prevEditor) continue;

            ICharacter otherCharacter = Character.get(player);
            if (otherCharacter == null) continue;

            CurrentSession otherSession = otherCharacter.getCurrentSession();
            if (otherSession.isViewing(session.editingType, session.editingId)) {
                if (first) {
                    otherSession.hold(session.editingType, session.editingId);
                    packet.editorName = player.getName();
                }

                Dispatcher.sendTo(packet, player);
                first = false;
            }
        }

        session.reset();
    }

    @Override
    public void run(EntityPlayerMP player, PacketContentExit message) {
        finishEditing(player);
    }
}