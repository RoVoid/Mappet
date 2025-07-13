package mchorse.mappet.api.scripts.code.data;

import mchorse.mappet.api.scripts.user.data.IScriptVector;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ScriptVector implements IScriptVector {

    public double x, y, z;

    public ScriptVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ScriptVector(Vec3d vector) {
        this.x = vector.x;
        this.y = vector.y;
        this.z = vector.z;
    }

    public ScriptVector(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    @Override
    public ScriptVector add(ScriptVector other) {
        return add(other.x, other.y, other.z);
    }

    @Override
    public ScriptVector add(double x, double y, double z) {
        return new ScriptVector(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public ScriptVector subtract(ScriptVector other) {
        return subtract(other.x, other.y, other.z);
    }

    @Override
    public ScriptVector subtract(double x, double y, double z) {
        return new ScriptVector(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public ScriptVector multiply(double scalar) {
        return new ScriptVector(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    @Override
    public ScriptVector multiply(ScriptVector other) {
        return new ScriptVector(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    @Override
    public ScriptVector divide(double scalar) {
        return new ScriptVector(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    @Override
    public ScriptVector divide(ScriptVector other) {
        return new ScriptVector(this.x / other.x, this.y / other.y, this.z / other.z);
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
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    @Override
    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
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
        return (this.x == x) && (this.y == y) && (this.z == z);
    }

    @Override
    public boolean equals(ScriptVector other) {
        return equals(other.x, other.y, other.z);
    }

    @Override
    public double angleBetween(ScriptVector other) {
        return other == null || isZero() || other.isZero() ? -1 : Math.acos(Math.max(-1.0, Math.min(1.0, normalize().dotProduct(other.normalize()))));
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
        return "ScriptVector(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}