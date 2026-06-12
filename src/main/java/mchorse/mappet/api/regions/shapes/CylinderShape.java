package mchorse.mappet.api.regions.shapes;

public class CylinderShape extends SphereShape {
    public CylinderShape() {
        super();
    }

    public CylinderShape(double horizontal, double vertical) {
        super(horizontal, vertical);
    }

    @Override
    public boolean isInside(double x, double y, double z) {
        double dx = x - offset.x;
        double dy = y - offset.y;
        double dz = z - offset.z;

        boolean isXZ = dx * dx + dz * dz <= horizontal * horizontal;
        boolean isY = Math.abs(dy) < vertical;

        return isXZ && isY;
    }

    @Override
    public String getType() {
        return "cylinder";
    }
}
