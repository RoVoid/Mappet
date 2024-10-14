package mchorse.mappet.api.scripts.user;

import mchorse.mappet.api.scripts.user.data.ScriptBox;
import mchorse.mappet.api.scripts.user.data.ScriptVector;

import javax.vecmath.*;

public interface IScriptMath {
    double random(double max);

    double random(double min, double max);

    double random(double min, double max, long seed);

    void seed(long seed);

    /**
     * Determines whether a point is located inside a bounding volume specified by two corners.
     * This method works with different vector types (2D, 3D, and 4D).
     *
     * <pre>{@code
     *   var pos = c.getSubject().getPosition();
     *   var point = mappet.vector3(pos.x, pos.y, pos.z);
     *   var bound1 = mappet.vector3(0, 0, 0);
     *   var bound2 = mappet.vector3(10, 10, 10);
     *   var isInside = mappet.isPointInBounds(point, bound1, bound2);
     *   c.send("Is the point inside the bounding volume? " + isInside);
     * }</pre>
     *
     * @param point The position of the point to check.
     * @param bound1 The position of one corner of the bounding volume.
     * @param bound2 The position of the opposite corner of the bounding volume.
     * @return true if the point is inside the bounding volume, false otherwise.
     * @throws IllegalArgumentException if the input vectors have different dimensions.
     */
    boolean isPointInBounds(Object point, Object bound1, Object bound2);

    double toDegrees(double radians);

    double toRadians(double degrees);

    double sign(double number);

    double factorial(double number);

    double gcd(double a, double b);

    double lcm(double a, double b);

    ScriptVector vector(double x, double y, double z);

    double angleBetweenVectors(ScriptVector vec1, ScriptVector vec2);

    Vector2d vector2();

    /**
     * Create a 2D vector.
     *
     * <pre>{@code
     *    var a = mappet.vector2(1, 0);
     *    var b = mappet.vector2(-1, 1);
     *
     *    a.normalize();
     *    b.normalize();
     *
     *    c.send("Dot product of a and b is: " + a.dot(b));
     * }</pre>
     */
    Vector2d vector2(double x, double y);

    Vector2d vector2(Vector2d v);

    Vector3d vector3();

    /**
     * Create a 3D vector.
     *
     * <pre>{@code
     *    var look = c.getSubject().getLook();
     *    var a = mappet.vector3(look.x, look.y, look.z);
     *    var b = mappet.vector3(0, 0, 1);
     *
     *    a.normalize();
     *    b.normalize();
     *
     *    c.send("Dot product of entity's look vector and positive Z is: " + a.dot(b));
     * }</pre>
     */
    Vector3d vector3(double x, double y, double z);

    Vector3d vector3(Vector3d v);

    Vector4d vector4();

    Vector4d vector4(double x, double y, double z, double w);

    Vector4d vector4(Vector4d v);

    /**
     * Create an identity 3x3 matrix.
     *
     * <pre>{@code
     *    var v = mappet.vector3(0, 0, 1);
     *    var rotation = mappet.matrix3();
     *
     *    rotation.rotY(Math.PI / 2);
     *    rotation.transform(v);
     *
     *    c.send("Final point is: " + v);
     * }</pre>
     */
    Matrix3d matrix3();

    Matrix3d matrix3(Matrix3d m);

    /**
     * Create an identity 4x4 matrix.
     *
     * <pre>{@code
     *    var v = mappet.vector4(0, 0, 1, 1);
     *    var rotation = mappet.matrix4();
     *
     *    rotation.rotY(Math.PI / 2);
     *
     *    var translation = mappet.matrix4();
     *
     *    translation.setTranslation(mappet.vector3(0, 4, 0));
     *    rotation.mul(translation);
     *    rotation.transform(v);
     *
     *    c.send("Final point is: " + v.x + ", " + v.y + ", " + v.z);
     * }</pre>
     */
    Matrix4d matrix4();

    Matrix4d matrix4(Matrix4d m);

    /**
     * Create a bounding box.
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var subject = c.getSubject();
     *     var subjectPosition = subject.getPosition();
     *     var box = mappet.box(-10, 4, -10, 10, 6, 10);
     *     if (box.contains(subjectPosition)){
     *         c.send("the player in in the box")
     *     }
     * }
     * }</pre>
     */
    ScriptBox box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
