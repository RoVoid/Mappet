package mchorse.mappet.api.regions.shapes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

public class SphereShape extends AbstractShape {
    public double horizontal = 1;
    public double vertical = 1;

    public SphereShape() {}

    public SphereShape(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    @Override
    public void from(AbstractShape shape) {
        super.from(shape);

        if (shape instanceof BoxShape) {
            horizontal = ((BoxShape) shape).size.x;
            vertical = ((BoxShape) shape).size.y;
        }
        else if (shape instanceof SphereShape) {
            horizontal = ((SphereShape) shape).horizontal;
            vertical = ((SphereShape) shape).vertical;
        }
    }

    @Override
    public AxisAlignedBB getSearchBox() {
        return new AxisAlignedBB(-horizontal + offset.x, -vertical + offset.y, -horizontal + offset.z, horizontal + offset.x,
                vertical + offset.y, horizontal + offset.z);
    }

    @Override
    public boolean isInside(double x, double y, double z) {
        double dx = (x - offset.x) / horizontal;
        double dy = (y - offset.y) / vertical;
        double dz = (z - offset.z) / horizontal;

        return dx * dx + dy * dy + dz * dz <= 1;
    }

    @Override
    public String getType() {
        return "sphere";
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();

        tag.setDouble("Horizontal", horizontal);
        tag.setDouble("Vertical", vertical);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);

        if (tag.hasKey("Horizontal")) horizontal = tag.getDouble("Horizontal");
        if (tag.hasKey("Vertical")) vertical = tag.getDouble("Vertical");
    }
}
