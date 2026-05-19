package mchorse.mappet.network.server.blocks;

import mchorse.mappet.network.packets.blocks.PacketEditTrigger;
import mchorse.mappet.blocks.tile.TileTrigger;
import mchorse.mappet.utils.PlayerUtils;
import mchorse.mappet.utils.WorldUtils;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

public class ServerHandlerEditTrigger extends ServerMessageHandler<PacketEditTrigger> {
    @Override
    public void run(EntityPlayerMP player, PacketEditTrigger message) {
        if (!PlayerUtils.isOperator(player)) return;

        TileEntity tile = WorldUtils.getTileEntity(player.world, message.pos);
        if (tile instanceof TileTrigger)
            ((TileTrigger) tile).set(message.left, message.right, message.collidable, message.boundingBoxPos1, message.boundingBoxPos2);
    }
}