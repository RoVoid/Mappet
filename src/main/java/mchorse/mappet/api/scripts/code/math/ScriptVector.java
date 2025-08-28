package mchorse.mappet.api.scripts.code.math;

import mchorse.mappet.api.scripts.user.math.IScriptVector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScriptVector implements IScriptVector {

    public double x, y, z;

    public static ScriptVector EMPTY = new ScriptVector(0, 0, 0);

    public ScriptVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ScriptVector(Vec3d vector) {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }

    public ScriptVector(BlockPos pos) {
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
    }

    @Override
    public ScriptVector add(double x, double y, double z) {
        return new ScriptVector(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public ScriptVector add(ScriptVector other) {
        return add(other.x, other.y, other.z);
    }

    @Override
    public ScriptVector subtract(double x, double y, double z) {
        return new ScriptVector(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public ScriptVector subtract(ScriptVector other) {
        return subtract(other.x, other.y, other.z);
    }

    @Override
    public ScriptVector multiply(double scalar) {
        return new ScriptVector(x * scalar, y * scalar, z * scalar);
    }

    @Override
    public ScriptVector multiply(ScriptVector other) {
        return new ScriptVector(x * other.x, y * other.y, z * other.z);
    }

    @Override
    public ScriptVector divide(double scalar) {
        return new ScriptVector(x / scalar, y / scalar, z / scalar);
    }

    @Override
    public ScriptVector divide(ScriptVector other) {
        return new ScriptVector(x / other.x, y / other.y, z / other.z);
    }

    @Override
    public ScriptVector floor() {
        return new ScriptVector(floorX(), floorY(), floorZ());
    }

    @Override
    public int floorX() {
        return (int) Math.floor(x);
    }

    @Override
    public int floorY() {
        return (int) Math.floor(y);
    }

    @Override
    public int floorZ() {
        return (int) Math.floor(z);
    }

    // Скалярная произведение
    @Override
    public double dotProduct(ScriptVector other) {
        return x * other.x + y * other.y + z * other.z;
    }

    // Векторное произведение
    @Override
    public ScriptVector crossProduct(ScriptVector other) {
        return new ScriptVector(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    @Override
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public double distance(ScriptVector other) {
        return distance(other.x, other.y, other.z);
    }

    @Override
    public double distance(double x, double y, double z) {
        double dx = x - this.x;
        double dy = y - this.y;
        double dz = z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public ScriptVector normalize() {
        return divide(length());
    }

    @Override
    public boolean equals(double x, double y, double z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public boolean equals(ScriptVector other) {
        return equals(other.x, other.y, other.z);
    }

    @Override
    public double angleBetween(ScriptVector other) {
        return other == null || isZero() || other.isZero() ? -1 : Math.acos(Math.max(-1.0,
                                                                                     Math.min(1.0,
                                                                                              normalize().dotProduct(other.normalize()))));
    }

    @Override
    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }

    @Override
    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public String toString() {
        return "ScriptVector(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScriptVector) {
            ScriptVector other =  (ScriptVector) obj;
            return x == other.x && y == other.y && z == other.z;
        }
        return super.equals(obj);
    }
}