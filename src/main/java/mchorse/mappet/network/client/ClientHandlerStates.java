package mchorse.mappet.network.client;

import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.network.packets.states.PacketStates;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerStates extends ClientMessageHandler<PacketStates>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketStates message)
    {
        Mappet.logger.debug(message.target, message.states.toString());
        GuiMappetDashboard.get(Minecraft.getMinecraft()).settings.fillStates(message.target, message.states);
    }
}