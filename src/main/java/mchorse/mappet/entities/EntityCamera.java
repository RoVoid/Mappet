package mchorse.mappet.entities;

import mchorse.mappet.capabilities.camera.Camera;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.PacketCamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityCamera extends Entity {
//    public EntityPlayer player;

    public EntityCamera(World world) {
        super(world);
        noClip = true;
        setSize(0.0F, 0.0F);
    }

    public EntityCamera(EntityPlayer owner) {
        super(owner.world);
        Camera.get(this).setPlayer(owner.getName());
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
//        if (nbt.hasKey("Player")) player = world.getPlayerEntityByName(nbt.getString("Player"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
//        if(player != null) nbt.setString("Player", player.getName());
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public void onKillCommand() {
        System.out.println("\"/kill\" doesn't kill cameras!");
        //super.onKillCommand();
    }

    @Override
    public void setDead() {
        super.setDead();

        if (!world.isRemote && !Camera.get(this).getPlayer().isEmpty()) {
            Dispatcher.sendTo(new PacketCamera(), (EntityPlayerMP) world.getPlayerEntityByName(Camera.get(this).getPlayer()));
        }
    }

}

