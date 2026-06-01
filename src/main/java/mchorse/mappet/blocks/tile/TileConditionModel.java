package mchorse.mappet.blocks.tile;

import mchorse.blockbuster.Blockbuster;
import mchorse.blockbuster.common.entity.EntityActor;
import mchorse.blockbuster.common.tileentity.TileEntityModelSettings;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.blocks.PacketEditConditionModel;
import mchorse.mappet.utils.ConditionModel;
import mchorse.mclib.math.Constant;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TileConditionModel extends TileEntity implements ITickable {
    public EntityActor entity;
    public int frequency;
    public boolean isGlobal = true;
    public boolean isShadow = false;
    private TileEntityModelSettings settings;
    public List<ConditionModel> list = new ArrayList<>();

    private int tick;

    public TileConditionModel() {
        frequency = 1;
        settings = new TileEntityModelSettings();
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        float range = Blockbuster.actorRenderingRange.get();

        return range * range;
    }

    @Override
    public void update() {
        if (world.isRemote) {
            updateMorph();
            return;
        }

        int frequency = Math.max(this.frequency, 1);

        Constant constantFalse = new Constant(0);

        if (tick % frequency == 0) for (EntityPlayer playerEntity : world.playerEntities) {
            AbstractMorph morph = null;

            for (ConditionModel conditionModel : list)
                if (conditionModel.condition.execute(new DataContext(playerEntity)) && !conditionModel.morph.equals(morph))
                    morph = conditionModel.morph;
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound tagMorph = new NBTTagCompound();
            if (morph != null) morph.toNBT(tagMorph);
            NBTTagCompound settings = new NBTTagCompound();
            this.settings.toNBT(settings);
            tag.setTag("settings", settings);
            tag.setTag("morph", tagMorph);
            tag.setBoolean("shadow", isShadow);
            tag.setBoolean("global", isGlobal);
            Dispatcher.sendTo(new PacketEditConditionModel(getPos(), tag).setIsEdit(false), (EntityPlayerMP) playerEntity);
        }

        tick += 1;
    }

    @SideOnly(Side.CLIENT)
    public void updateMorph() {
        if (entity == null) createEntity(world);

        if (entity.morph.get() != null) entity.morph.get().update(entity);

        ++entity.ticksExisted;
    }

    public TileEntityModelSettings getSettings() {
        return settings;
    }

    public void createEntity(World world) {
        if (world != null) {
            entity = new EntityActor(world);
            entity.onGround = true;
        }
    }

    public void fill(NBTTagCompound tag) {
        list.clear();

        NBTTagList list = tag.getTagList("list", 10);

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound element = list.getCompoundTagAt(i);

            ConditionModel conditionModel = new ConditionModel();
            conditionModel.deserializeNBT(element);

            this.list.add(conditionModel);
        }

        frequency = tag.getInteger("frequency");
        isGlobal = tag.getBoolean("global");
        isShadow = tag.getBoolean("shadow");
    }

    public NBTTagCompound toNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();

        for (ConditionModel element : this.list) list.appendTag(element.serializeNBT());

        tag.setTag("list", list);
        tag.setInteger("frequency", frequency);
        tag.setBoolean("global", isGlobal);
        tag.setBoolean("shadow", isShadow);
        return tag;
    }

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
        NBTTagCompound settings = new NBTTagCompound();
        this.settings.toNBT(settings);

        tag.setTag("settings", settings);

        toNBT(tag);

        return super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        settings = new TileEntityModelSettings();
        settings.fromNBT((NBTTagCompound) tag.getTag("settings"));

        fill(tag);
    }
}
