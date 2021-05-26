package mchorse.mappet.utils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldUtils
{
    public static TileEntity getTileEntity(World world, BlockPos pos)
    {
        if (world.isBlockLoaded(pos))
        {
            return world.getTileEntity(pos);
        }

        return null;
    }

    public static void playSound(EntityPlayerMP player, String soundEvent)
    {
        player.connection.sendPacket(new SPacketCustomSound(soundEvent, SoundCategory.MASTER, player.posX, player.posY, player.posZ, 1, 1));
    }
}