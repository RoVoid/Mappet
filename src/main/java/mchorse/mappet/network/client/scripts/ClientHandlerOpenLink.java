package mchorse.mappet.network.client.scripts;

import mchorse.mappet.client.RenderingHandler;
import mchorse.mappet.client.gui.utils.GuiWebUtils;
import mchorse.mappet.network.common.scripts.PacketOpenLink;
import mchorse.mappet.network.common.scripts.PacketWorldMorph;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerOpenLink extends ClientMessageHandler<PacketOpenLink>
{
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketOpenLink message)
    {
        GuiWebUtils utils = new GuiWebUtils();
        utils.requestToOpenWebLink(message.getLink());
    }
}