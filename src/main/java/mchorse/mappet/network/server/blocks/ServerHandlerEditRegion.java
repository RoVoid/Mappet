package mchorse.mappet.network.server.blocks;

import mchorse.mappet.network.packets.blocks.PacketEditRegion;
import mchorse.mappet.blocks.tile.TileRegion;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mappet.utils.WorldUtils;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

public class ServerHandlerEditRegion extends ServerMessageHandler<PacketEditRegion> {
    @Override
    public void run(EntityPlayerMP player, PacketEditRegion message) {
        if (!PlayerUtils.isOperator(player)) return;

        TileEntity tile = WorldUtils.getTileEntity(player.world, message.pos);
        if (tile instanceof TileRegion) ((TileRegion) tile).set(message.tag);
    }
}