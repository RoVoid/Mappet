package mchorse.mappet.blocks.tile;

import mchorse.mappet.api.regions.Region;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.utils.PositionCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

public class TileRegion extends TileEntity implements ITickable {
    public Region region = new Region();


    private final Set<UUID> entities = new HashSet<>(10);
    private final Map<UUID, MutableInt> delays = new HashMap<>();
    private int tick;

    public void set(NBTTagCompound tag) {
//        if (tag == null || tag.hasNoTags()) region = new Region();
        region.deserializeNBT(tag);
        markDirty();

    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (!delays.isEmpty()) checkDelays();

        int frequency = Math.max(region.update, 1);
        if (tick % frequency == 0) {
            checkRegion();
            tick = 0;
        }
        ++tick;
    }

    private void checkDelays() {
        delays.entrySet().removeIf(entry -> {
            int delay = entry.getValue().decrementAndGet();
            if (delay > 0) {
                entry.getValue().setValue(delay);
                return false;
            }
            EntityPlayer player = world.getPlayerEntityByUUID(entry.getKey());
            if (player != null) region.triggerEnter(player, getPos());
            return true;
        });
    }

    private void checkRegion() {
        List<? extends Entity> list = region.checkEntities ? world.loadedEntityList : world.playerEntities;
        for (Entity entity : list) {
            UUID id = entity.getUniqueID();
            boolean wasInside = entities.contains(id);

            if (region.isEntityInside(entity, getPos())) {
                if (!region.isEnabled(entity)) {
                    if (!region.passable && entity instanceof EntityPlayer) handlePassing((EntityPlayer) entity);
                    continue;
                }

                region.triggerTick(entity, getPos());

                if (wasInside) continue;

                if (region.delay > 0) delays.put(id, new MutableInt(region.delay));
                else region.triggerEnter(entity, getPos());
                entities.add(id);
            }
            else if (wasInside) {
                if (delays.remove(id) == null) region.triggerExit(entity, getPos());
                entities.remove(id);
            }
        }
    }

    private void handlePassing(EntityPlayer player) {
        ICharacter character = Character.get(player);
        if (character == null) return;

        // out last
        Vec3d last = player.getPositionVector();


        PositionCache cache = character.getPositionCache();
        Vec3d vec = cache.last10Position;
        if (vec == null) return;

        if (region.isEntityOutside(vec.x, vec.y + player.height / 2, vec.z, getPos())) {
            teleportEntity(player, vec, last);

            cache.resetLastPositionTimer();

            return;
        }

//        vec = cache.lastLastPosition;
//        if (vec != null && region.isEntityOutside(vec.x, vec.y + player.height / 2, vec.z, getPos())) {
//            teleportEntity(player, vec, last);
//            cache.resetLastPositionTimer();
//            return;
//        }

        vec = vec.subtract(player.posX, player.posY, player.posZ);

        if (vec.lengthSquared() > 0) {
            vec = vec.normalize().scale(-0.5D);

            double x = player.posX;
            double y = player.posY;
            double z = player.posZ;

            while (region.isEntityInside(player, getPos())) {
                player.posX += vec.x;
                player.posY += vec.y;
                player.posZ += vec.z;
            }

            vec = new Vec3d(x, y, z);

            player.posX = x;
            player.posY = y;
            player.posZ = z;

            teleportEntity(player, vec, last);
        }

        cache.resetLastPositionTimer();
    }

    private void teleportEntity(Entity entity, Vec3d vec, Vec3d last) {
        entity.setPositionAndUpdate(vec.x, vec.y, vec.z);

        Vec3d motion = last.subtract(entity.getPositionVector()).scale(-0.5);

        if (motion.distanceTo(Vec3d.ZERO) < 0.5 * 0.5) motion = motion.normalize();

        double y = Math.abs(motion.y) < 0.01 ? 0.2 : motion.y;

        ((EntityPlayerMP) entity).connection.sendPacket(new SPacketEntityVelocity(entity.getEntityId(), motion.x, y, motion.z));
    }

    /* Rendering related stuff */

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        float range = 128;
        return range * range;
    }

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