package mchorse.mappet.blocks.tile;

import mchorse.mappet.api.regions.Region;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class TileRegion extends TileEntity implements ITickable {
    public Region region = new Region();

    private Set<UUID> entities = new HashSet<>(10);
    private Set<UUID> previous = new HashSet<>();

    private final Map<UUID, Integer> delays = new HashMap<>();
    private int tick;

    public void set(NBTTagCompound tag) {
        region.deserializeNBT(tag);
        markDirty();
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (!delays.isEmpty()) checkDelays();

        int frequency = Math.max(region.update, 1);
        if (tick % frequency == 0) checkRegion();
        ++tick;
    }

    private void checkDelays() {
        Iterator<Map.Entry<UUID, Integer>> it = delays.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();

            if (tick < entry.getValue()) continue;

            EntityPlayer player = world.getPlayerEntityByUUID(entry.getKey());
            if (player != null) region.triggerEnter(player, getPos());
            it.remove();
        }
    }

    private void checkRegion() {
        List<? extends Entity> list = world.getEntitiesWithinAABB(region.checkEntities ? EntityLivingBase.class : EntityPlayer.class,
                region.getSearchBox(getPos()));

        Set<UUID> tmp = previous;
        previous = entities;
        entities = tmp;
        entities.clear();

        for (Entity entity : list) {
            UUID id = entity.getUniqueID();
            if (!region.isEntityInside(entity, getPos()) || !region.isEnabled(entity, previous.contains(id))) continue;

            region.triggerTick(entity, getPos());
            entities.add(id);

            if (previous.remove(id)) continue; // was in region

            if (region.delay > 0) delays.put(id, tick + region.delay);
            else region.triggerEnter(entity, getPos());
        }

        // remove unchecked entities not in region
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return;
        for (UUID id : previous) {
            delays.remove(id);
            Entity entity = server.getEntityFromUuid(id);
            if (entity != null) region.triggerExit(entity, getPos());
        }
    }

    /* Rendering related stuff */

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    //    @Override
    //    @SideOnly(Side.CLIENT)
    //    public double getMaxRenderDistanceSquared() {
    //        float range = 128;
    //        return range * range;
    //    }

    /* NBT stuff */

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setTag("Region", region.serializeNBT());
        return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("Region")) region.deserializeNBT(tag.getCompoundTag("Region"));
    }
}