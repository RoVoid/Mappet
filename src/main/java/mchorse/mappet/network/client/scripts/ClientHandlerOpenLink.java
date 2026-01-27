package mchorse.mappet.network.client.scripts;

import mchorse.mappet.client.gui.utils.SafeWebLinkOpener;
import mchorse.mappet.network.packets.scripts.PacketOpenLink;
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
        SafeWebLinkOpener utils = new SafeWebLinkOpener();
        utils.requestToOpenWebLink(message.getLink());
    }
}