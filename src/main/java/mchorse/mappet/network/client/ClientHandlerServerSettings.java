package mchorse.mappet.network.client;

import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.network.packets.settings.PacketServerSettings;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerServerSettings extends ClientMessageHandler<PacketServerSettings>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketServerSettings message)
    {
        GuiMappetDashboard.get(Minecraft.getMinecraft()).settings.fill(message.tag);
    }
}