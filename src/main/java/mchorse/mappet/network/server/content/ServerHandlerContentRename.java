package mchorse.mappet.network.server.content;

import mchorse.mappet.api.utils.manager.IManager;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentPaths;
import mchorse.mappet.network.packets.content.PacketContentRename;
import mchorse.mappet.utils.CurrentSession;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerContentRename extends ServerMessageHandler<PacketContentRename> {

    @Override
    public void run(EntityPlayerMP player, PacketContentRename message) {
        if (message.path == null || message.newPath == null || message.path.isEmpty()) return;

        if (!PlayerUtils.isOperator(player)) return;

        ICharacter character = Character.get(player);
        if (character == null) return;

        CurrentSession session = character.getCurrentSession();
        boolean isEditing = session.isEditing(message.type, message.path);

        if (!isEditing) {
            return;
        }

        IManager<?> manager = message.type.manager();

        if (!manager.rename(message.path, message.newPath)) {
            Dispatcher.sendTo(new PacketContentRename(message.type, message.newPath, message.path), player);
            return;
        }

        PacketContentPaths namesPacket = new PacketContentPaths(message.type, manager.getPaths()).rename(message.path, message.newPath);

        if (player.getServer() == null) return;

        for (EntityPlayerMP otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            Dispatcher.sendTo(namesPacket, otherPlayer);

            if (otherPlayer == player) continue;

            ICharacter otherCharacter = Character.get(otherPlayer);
            if (otherCharacter == null) continue;

            CurrentSession otherSession = otherCharacter.getCurrentSession();
            if (otherSession.isViewing(message.type, message.path))
                Dispatcher.sendTo(new PacketContentRename(message.type, message.path, message.newPath), otherPlayer);
        }
    }
}