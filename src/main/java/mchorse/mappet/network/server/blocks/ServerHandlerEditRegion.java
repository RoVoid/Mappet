package mchorse.mappet.network.server.blocks;

import mchorse.mappet.network.packets.blocks.PacketEditRegion;
import mchorse.mappet.tile.TileRegion;
import mchorse.mappet.utils.WorldUtils;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

public class ServerHandlerEditRegion extends ServerMessageHandler<PacketEditRegion>
{
    @Override
    public void run(EntityPlayerMP player, PacketEditRegion message)
    {
        MinecraftServer server = player.getServer();
        if (server != null && !server.getPlayerList().canSendCommands(player.getGameProfile())) return;

        TileEntity tile = WorldUtils.getTileEntity(player.world, message.pos);

        if (tile instanceof TileRegion)
        {
            ((TileRegion) tile).set(message.tag);
        }
    }
}