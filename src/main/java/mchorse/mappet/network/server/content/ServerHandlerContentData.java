package mchorse.mappet.network.server.content;

import mchorse.mappet.api.utils.manager.IManager;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentData;
import mchorse.mappet.network.packets.content.PacketContentPaths;
import mchorse.mappet.utils.CurrentSession;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerContentData extends ServerMessageHandler<PacketContentData> {
    @Override
    public void run(EntityPlayerMP player, PacketContentData message) {
        if (!PlayerUtils.isOperator(player)) return;

        ICharacter character = Character.get(player);
        if (character == null) return;

        CurrentSession session = character.getCurrentSession();
        boolean isEditing = session.isEditing(message.type, message.path);

        IManager<?> manager = message.type.manager();
        boolean exists = manager.exists(message.path);

        if (!isEditing && exists) return;

        if (message.data == null) { // delete
            manager.delete(message.path);
            message.editorName = null;

            session.reset();
        }
        else { // create & edit
            manager.save(message.path, message.data);
            message.editorName = player.getName();

            session.hold(message.type, message.path);
            session.observe(message.type, message.path);
        }

        PacketContentPaths packet = null;
        if (exists != manager.exists(message.path)) { // delete & create
            packet = new PacketContentPaths(message.type, manager.getPaths(), message.requestId);
            Dispatcher.sendTo(packet, player);
        }

        if (player.getServer() == null) return;
        for (EntityPlayerMP otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer == player) continue;

            ICharacter otherCharacter = Character.get(otherPlayer);
            if (otherCharacter == null) continue;

            CurrentSession otherSession = otherCharacter.getCurrentSession();
            if (otherSession.viewingType == message.type && packet != null) Dispatcher.sendTo(packet, otherPlayer);
            if (otherSession.isViewing(message.type, message.path)) Dispatcher.sendTo(message, otherPlayer);
        }
    }
}