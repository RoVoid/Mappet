package mchorse.mappet.api.scripts.user.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Script vector (position) represents a position in the space
 */
public class ScriptVector {
    /**
     * X coordinate
     */
    public double x;

    /**
     * Y coordinate
     */
    public double y;

    /**
     * Z coordinate
     */
    public double z;

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
    public String toString() {
        return "ScriptVector(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    /**
     * Convert this vector to an array string
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var subject = c.getSubject();
     *     var subjectPosition = subject.getPosition();
     *     c.send("The player is at " + subjectPosition.toArrayString() + "!");
     *     // The player is at [x, y, z]!
     * }
     * }</pre>
     */
    public String toArrayString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    public ScriptVector add(ScriptVector other) {
        return add(other.x, other.y, other.z);
    }

    public ScriptVector add(double x, double y, double z) {
        return new ScriptVector(this.x + x, this.y + y, this.z + z);
    }

    public ScriptVector multiply(double scalar) {
        return new ScriptVector(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public ScriptVector multiply(ScriptVector other) {
        return new ScriptVector(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double distance(ScriptVector other) {
        return distance(other.x, other.y, other.z);
    }

    public double distance(double x, double y, double z) {
        double dx = x - this.x;
        double dy = y - this.y;
        double dz = z - this.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public ScriptVector normalize() {
        double length = this.length();
        return new ScriptVector(this.x / length, this.y / length, this.z / length);
    }
}