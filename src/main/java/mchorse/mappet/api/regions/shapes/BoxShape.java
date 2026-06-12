package mchorse.mappet.api.regions.shapes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import javax.vecmath.Vector3d;

public class BoxShape extends AbstractShape {
    public Vector3d size = new Vector3d(1, 1, 1);

    @Override
    public void from(AbstractShape shape) {
        super.from(shape);

        if (shape instanceof BoxShape) size.set(((BoxShape) shape).size);
        else if (shape instanceof SphereShape) {
            double h = ((SphereShape) shape).horizontal;
            double v = ((SphereShape) shape).vertical;
            size.set(h, v, h);
        }
    }

    @Override
    public AxisAlignedBB getSearchBox() {
        return new AxisAlignedBB(-size.x + offset.x, -size.y + offset.y, -size.z + offset.z, size.x + offset.x, size.y + offset.y, size.z + offset.z);
    }

    @Override
    public boolean isInside(double x, double y, double z) {
        double dx = x - offset.x;
        double dy = y - offset.y;
        double dz = z - offset.z;

        return Math.abs(dx) < size.x && Math.abs(dy) < size.y && Math.abs(dz) < size.z;
    }

    @Override
    public String getType() {
        return "box";
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();

        tag.setDouble("SizeX", size.x);
        tag.setDouble("SizeY", size.y);
        tag.setDouble("SizeZ", size.z);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);

        if (tag.hasKey("SizeX") && tag.hasKey("SizeY") && tag.hasKey("SizeZ")) {
            size.x = tag.getDouble("SizeX");
            size.y = tag.getDouble("SizeY");
            size.z = tag.getDouble("SizeZ");
        }
    }
}