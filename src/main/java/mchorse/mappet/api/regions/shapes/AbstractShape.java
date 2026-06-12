package mchorse.mappet.api.regions.shapes;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import javax.vecmath.Vector3d;

/**
 * Abstract shape class
 * <p>
 * This base class provides base of operation for region's shapes
 */
public abstract class AbstractShape implements INBTSerializable<NBTTagCompound> {
    public Vector3d offset = new Vector3d();

    public static AbstractShape create(String string) {
        switch (string) {
            case "box":
                return new BoxShape();
            case "sphere":
                return new SphereShape();
            case "cylinder":
                return new CylinderShape();
        }
        return null;
    }

    public void from(AbstractShape shape) {
        offset.set(shape.offset);
    }

    public boolean isEntityInside(Entity entity, BlockPos tile) {
        return offset != null && isInside(entity.posX, entity.posY, entity.posZ, tile);
    }

    public boolean isInside(double x, double y, double z, BlockPos tile) {
        return offset != null && isInside(x - tile.getX() - 0.5, y - tile.getY() - 0.5, z - tile.getZ() - 0.5);
    }

    public abstract String getType();

    public abstract boolean isInside(double x, double y, double z);

    public abstract AxisAlignedBB getSearchBox();

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        tag.setString("Type", getType());
        tag.setDouble("PosX", offset.x);
        tag.setDouble("PosY", offset.y);
        tag.setDouble("PosZ", offset.z);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag.hasKey("PosX") && tag.hasKey("PosY") && tag.hasKey("PosZ")){
            offset.x = tag.getDouble("PosX");
            offset.y = tag.getDouble("PosY");
            offset.z = tag.getDouble("PosZ");
        }
    }
}