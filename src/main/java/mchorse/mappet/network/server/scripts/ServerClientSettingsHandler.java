package mchorse.mappet.network.server.scripts;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.ScriptEvent;
import mchorse.mappet.api.scripts.code.nbt.ScriptNBTCompound;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.network.packets.scripts.PacketClientSettings;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

import javax.script.ScriptException;

public class ServerClientSettingsHandler extends ServerMessageHandler<PacketClientSettings> {
    @Override
    public void run(EntityPlayerMP player, PacketClientSettings message) {
        if (message.script == null || message.script.isEmpty()) return;
        try {
            DataContext context = new DataContext(player);
            Mappet.scripts.execute(message.script,
                                   message.function == null || message.function.isEmpty() ? "handler" : message.function,
                                   context,
                                   new ScriptEvent(context, message.script, message.function),
                                   new ScriptNBTCompound(message.options));
        } catch (ScriptException | NoSuchMethodException e) {
            Mappet.logger.error(e.getMessage());
        }
    }
}
