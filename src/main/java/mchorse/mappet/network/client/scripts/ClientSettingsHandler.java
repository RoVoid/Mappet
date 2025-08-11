package mchorse.mappet.network.client.scripts;

import mchorse.mappet.api.utils.ClientSettingsAccessor;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.scripts.PacketClientSettings;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientSettingsHandler extends ClientMessageHandler<PacketClientSettings> {
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketClientSettings message) {
        ClientSettingsAccessor accessor = new ClientSettingsAccessor();
        NBTTagCompound result = accessor.parse(message.requests, message.options);

        if (message.script == null || message.script.isEmpty()) return;
        PacketClientSettings response = new PacketClientSettings(null, result, message.script, message.function);
        Dispatcher.sendToServer(response);
    }
}