package mchorse.mappet.api.scripts.code;

import mchorse.mappet.api.scripts.user.IScriptMath;
import mchorse.mappet.api.scripts.user.data.ScriptBox;
import mchorse.mappet.api.scripts.user.data.ScriptVector;

import javax.vecmath.*;
import java.util.Random;

public class ScriptMath implements IScriptMath {
    private final Random random = new Random();

    /**
     * Generate a random number between 0 and the given max value (but not
     * including the maximum value).
     *
     * <pre>{@code
     *    var randomNumber = mappet.random(10);
     *
     *    c.send(randomNumber);
     * }</pre>
     *
     * @param max Maximum value.
     */
    @Override
    public double random(double max) {
        return Math.random() * max;
    }

    @Override
    public double random(double min, double max) {
        return min + Math.random() * (max - min);
    }

    @Override
    public double random(double min, double max, long seed) {
        this.random.setSeed(seed);
        return min + this.random.nextDouble() * (max - min);
    }

    @Override
    public void seed(long seed) {
        this.random.setSeed(seed);
    }

    @Override
    public boolean isPointInBounds(Object point, Object bound1, Object bound2) {
        if (point instanceof Vector2d) {
            return this.isPointInBounds2D((Vector2d) point, (Vector2d) bound1, (Vector2d) bound2);
        } else if (point instanceof Vector3d) {
            return this.isPointInBounds3D((Vector3d) point, (Vector3d) bound1, (Vector3d) bound2);
        } else if (point instanceof Vector4d) {
            return this.isPointInBounds4D((Vector4d) point, (Vector4d) bound1, (Vector4d) bound2);
        } else {
            throw new IllegalArgumentException("Invalid vector type: " + point.getClass().getName());
        }
    }

    public boolean isPointInBounds2D(Vector2d point, Vector2d bound1, Vector2d bound2) {
        return point.x >= Math.min(bound1.x, bound2.x) && point.x <= Math.max(bound1.x, bound2.x) && point.y >= Math.min(bound1.y, bound2.y) && point.y <= Math.max(bound1.y, bound2.y);
    }

    public boolean isPointInBounds3D(Vector3d point, Vector3d bound1, Vector3d bound2) {
        return point.x >= Math.min(bound1.x, bound2.x) && point.x <= Math.max(bound1.x, bound2.x) && point.y >= Math.min(bound1.y, bound2.y) && point.y <= Math.max(bound1.y, bound2.y) && point.z >= Math.min(bound1.z, bound2.z) && point.z <= Math.max(bound1.z, bound2.z);
    }

    public boolean isPointInBounds4D(Vector4d point, Vector4d bound1, Vector4d bound2) {
        return point.x >= Math.min(bound1.x, bound2.x) && point.x <= Math.max(bound1.x, bound2.x) && point.y >= Math.min(bound1.y, bound2.y) && point.y <= Math.max(bound1.y, bound2.y) && point.z >= Math.min(bound1.z, bound2.z) && point.z <= Math.max(bound1.z, bound2.z) && point.w >= Math.min(bound1.w, bound2.w) && point.w <= Math.max(bound1.w, bound2.w);
    }

    @Override
    public double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    @Override
    public double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    @Override
    public double sign(double number) {
        return Math.signum(number);
    }

    @Override
    public double factorial(double number) {
        return (number != 1) ? number * factorial(number - 1) : 1;
    }

    @Override
    public double gcd(double a, double b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }

    @Override
    public double lcm(double a, double b) {
        return (a * b) / gcd(a, b);
    }

    /* Vector math */

    @Override
    public ScriptVector vector(double x, double y, double z) {
        return new ScriptVector(x, y, z);
    }

    @Override
    public double angleBetweenVectors(ScriptVector vec1, ScriptVector vec2) {
        return Math.acos(vec1.dotProduct(vec2));
    }

    @Override
    public Vector2d vector2() {
        return new Vector2d();
    }

    @Override
    public Vector2d vector2(double x, double y) {
        return new Vector2d(x, y);
    }

    @Override
    public Vector2d vector2(Vector2d v) {
        return new Vector2d(v);
    }

    @Override
    public javax.vecmath.Vector3d vector3() {
        return new javax.vecmath.Vector3d();
    }

    @Override
    public Vector3d vector3(double x, double y, double z) {
        return new Vector3d(x, y, z);
    }

    @Override
    public Vector3d vector3(Vector3d v) {
        return new Vector3d(v);
    }

    @Override
    public Vector4d vector4() {
        return new Vector4d();
    }

    @Override
    public Vector4d vector4(double x, double y, double z, double w) {
        return new Vector4d(x, y, z, w);
    }

    @Override
    public Vector4d vector4(Vector4d v) {
        return new Vector4d(v);
    }

    @Override
    public Matrix3d matrix3() {
        Matrix3d m = new Matrix3d();
        m.setIdentity();
        return m;
    }

    @Override
    public Matrix3d matrix3(Matrix3d m) {
        return new Matrix3d(m);
    }

    @Override
    public Matrix4d matrix4() {
        Matrix4d m = new Matrix4d();
        m.setIdentity();
        return m;
    }

    @Override
    public Matrix4d matrix4(Matrix4d m) {
        return new Matrix4d(m);
    }

    @Override
    public ScriptBox box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new ScriptBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
