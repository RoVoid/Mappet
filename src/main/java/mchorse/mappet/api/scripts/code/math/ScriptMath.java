package mchorse.mappet.api.scripts.code.math;

import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class ScriptMath {
    private final Random random;
    private long seed;

    public ScriptMath() {
        seed = System.nanoTime() ^ System.currentTimeMillis();
        random = new Random(seed);
    }

    public double random() {
        return random.nextDouble();
    }

    public double random(double max) {
        return random.nextDouble() * max;
    }

    public double random(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    @Deprecated
    public void seed(long newSeed) {
        seed = newSeed;
        random.setSeed(newSeed);
    }

    public void setSeed(long newSeed) {
        seed = newSeed;
        random.setSeed(newSeed);
    }

    public long getSeed() {
        return seed;
    }

    public double floor(double value) {
        return Math.floor(value);
    }

    public double floor(double value, int precision) {
        double factor = Math.pow(10, precision);
        return Math.floor(value * factor) / factor;
    }

    public double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    public double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    public double sign(double number) {
        return Math.signum(number);
    }

    public double factorial(double number) {
        return number != 1 ? number * factorial(number - 1) : 1;
    }

    public double gcd(double a, double b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }

    public double lcm(double a, double b) {
        return a * b / gcd(a, b);
    }

    public ScriptVector vector() {
        return ScriptVector.EMPTY;
    }

    public ScriptVector vector(double x, double y, double z) {
        return new ScriptVector(x, y, z);
    }

    public ScriptVector vector(BlockPos pos) {
        return new ScriptVector(pos);
    }

    public ScriptBox box() {
        return ScriptBox.EMPTY;
    }

    public ScriptBox box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new ScriptBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public ScriptBox box(ScriptVector vec1, ScriptVector vec2) {
        return new ScriptBox(vec1, vec2);
    }
}
