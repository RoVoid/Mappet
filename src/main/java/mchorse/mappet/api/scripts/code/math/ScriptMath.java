package mchorse.mappet.api.scripts.code.math;

import mchorse.mappet.api.scripts.user.math.IScriptMath;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class ScriptMath implements IScriptMath {
    private final Random random;
    private long seed;

    public ScriptMath() {
        seed = System.nanoTime() ^ System.currentTimeMillis();
        random = new Random(seed);
    }

    @Override
    public double random() {
        return random.nextDouble();
    }

    @Override
    public double random(double max) {
        return random.nextDouble() * max;
    }

    @Override
    public double random(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    @Override
    @Deprecated
    public void seed(long newSeed) {
        seed = newSeed;
        random.setSeed(newSeed);
    }

    @Override
    public void setSeed(long newSeed) {
        seed = newSeed;
        random.setSeed(newSeed);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public double floor(double value) {
        return Math.floor(value);
    }

    @Override
    public double floor(double value, int precision) {
        double factor = Math.pow(10, precision);
        return Math.floor(value * factor) / factor;
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
        return number != 1 ? number * factorial(number - 1) : 1;
    }

    @Override
    public double gcd(double a, double b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }

    @Override
    public double lcm(double a, double b) {
        return a * b / gcd(a, b);
    }

    @Override
    public ScriptVector vector() {
        return ScriptVector.EMPTY;
    }

    @Override
    public ScriptVector vector(double x, double y, double z) {
        return new ScriptVector(x, y, z);
    }

    @Override
    public ScriptVector vector(BlockPos pos) {
        return new ScriptVector(pos);
    }

    @Override
    public ScriptBox box() {
        return ScriptBox.EMPTY;
    }

    @Override
    public ScriptBox box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new ScriptBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public ScriptBox box(ScriptVector vec1, ScriptVector vec2) {
        return new ScriptBox(vec1, vec2);
    }
}
