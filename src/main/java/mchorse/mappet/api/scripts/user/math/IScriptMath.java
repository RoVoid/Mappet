package mchorse.mappet.api.scripts.user.math;

import mchorse.mappet.api.scripts.code.math.ScriptBox;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import net.minecraft.util.math.BlockPos;

public interface IScriptMath {
    ScriptBox box(ScriptVector vec1, ScriptVector vec2);

    /**
     * Generate a number between 0 and the given max value (but not including the max value)
     *
     * <pre>{@code
     *    var randomNumber = mappet.random(10); // [0, 10)
     *    c.send(randomNumber);
     * }</pre>
     */
    double random(double max);

    /**
     * Generate a number between given min value and the given max value (but not including the max value)
     *
     * <pre>{@code
     *    var randomNumber = mappet.random(-3, 10); // [-3, 10)
     *    c.send(randomNumber);
     * }</pre>
     */
    double random(double min, double max);

    /**
     * @deprecated Use {@link #setSeed(long)} instead
     */
    void seed(long seed);

    /**
     * Set seed for randomizer
     */
    void setSeed(long newSeed);

    /**
     * Get randomizer seed
     */
    long getSeed();

    /**
     * <pre>{@code
     *    c.send(math.floor(3.14)); // 3
     * }</pre>
     */
    double floor(double value);

    /**
     * <pre>{@code
     *    c.send(math.floor(3.14, 1)); // 3.1
     * }</pre>
     */
    double floor(double value, int precision);

    /**
     * Convert radians to degrees
     */
    double toDegrees(double radians);

    /**
     * Convert degrees to radians
     */
    double toRadians(double degrees);

    double sign(double number);

    double factorial(double number);

    double gcd(double a, double b);

    double lcm(double a, double b);

    /**
     * Create a scripted vector.
     */
    ScriptVector vector();
    ScriptVector vector(double x, double y, double z);

    ScriptVector vector(BlockPos pos);

    ScriptBox box();

    /**
     * Create a bounding box.
     *
     * <pre>{@code
     * function main(c)
     * {
     *     var pos = c.getSubject().getPosition();
     *     var box = math.box(-10, 4, -10, 10, 6, 10);
     *     if (box.contains(pos)){
     *         c.send("Player in the box")
     *     }
     * }
     * }</pre>
     */
    ScriptBox box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
