package mchorse.mappet.network.server.content;

import mchorse.mappet.api.utils.content.IContentTypeBase;
import mchorse.mappet.api.utils.manager.IManager;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentFolder;
import mchorse.mappet.network.packets.content.PacketContentPaths;
import mchorse.mappet.utils.CurrentSession;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerHandlerContentFolder extends ServerMessageHandler<PacketContentFolder> {

    @Override
    public void run(EntityPlayerMP player, PacketContentFolder message) {
        IManager<?> manager = message.type.manager();

        PacketContentPaths packet;

        if (message.rename != null && !message.path.isEmpty()) {
            int lastIndex = message.path.lastIndexOf('/');
            String newPath = lastIndex == -1 ? message.rename : message.path.substring(0, lastIndex + 1) + message.rename;
            manager.renameFolder(message.path, newPath);
            packet = new PacketContentPaths(message.type, manager.getPaths()).rename(message.path, newPath);
        }
        else if (message.delete && !message.path.isEmpty()) {
            String prefix = message.path.endsWith("/") ? message.path : message.path + "/";
            if (isAnyoneWorkingIn(player, message.type, prefix)) return;
            manager.deleteFolder(message.path);
            packet = new PacketContentPaths(message.type, manager.getPaths());
        }
        else {
            manager.addFolder(message.path);
            packet = new PacketContentPaths(message.type, manager.getPaths());
        }

        if (packet != null && player.getServer() != null) for (EntityPlayerMP otherPlayer : player.getServer().getPlayerList().getPlayers())
            Dispatcher.sendTo(packet, otherPlayer);
    }

    private boolean isAnyoneWorkingIn(EntityPlayerMP except, IContentTypeBase type, String prefix) {
        if (except.getServer() != null) for (EntityPlayerMP other : except.getServer().getPlayerList().getPlayers()) {
            if (other == except) continue;
            ICharacter character = Character.get(other);
            if (character == null) continue;
            CurrentSession session = character.getCurrentSession();
            if (session.viewingType != type) continue;
            if (session.viewingId.startsWith(prefix)) return true;
        }
        return false;
    }
}