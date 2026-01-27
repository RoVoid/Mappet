package mchorse.mappet.network.client.scripts;

import mchorse.mappet.network.packets.scripts.PacketClipboard;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerClipboard extends ClientMessageHandler<PacketClipboard>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketClipboard message)
    {
        GuiScreen.setClipboardString(message.clipboard);
    }
}