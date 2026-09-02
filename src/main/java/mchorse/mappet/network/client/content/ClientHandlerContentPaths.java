package mchorse.mappet.network.client.content;

import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.panels.GuiMappetDashboardPanel;
import mchorse.mappet.network.packets.content.PacketContentPaths;
import mchorse.mappet.proxy.ClientProxy;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerContentPaths extends ClientMessageHandler<PacketContentPaths> {
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketContentPaths message) {
        if (message.requestId >= 0) {
            ClientProxy.process(message.paths, message.requestId);
            return;
        }

        GuiMappetDashboard dashboard = GuiMappetDashboard.get(Minecraft.getMinecraft());
        GuiMappetDashboardPanel<?> panel = message.type.panel(dashboard);

        if (panel != null) panel.fillPaths(message.paths, message.renameOld, message.renameNew);
    }
}