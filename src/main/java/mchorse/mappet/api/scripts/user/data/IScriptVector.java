package mchorse.mappet.api.scripts.user.data;

import mchorse.mappet.api.scripts.code.data.ScriptVector;
import net.minecraft.util.math.BlockPos;

/**
 * Script Vector
 * <p> CREATE: {@link mchorse.mappet.api.scripts.user.IScriptMath#vector(double, double, double)} </p>
 */
public interface IScriptVector {
    ScriptVector add(ScriptVector other);

    ScriptVector add(double x, double y, double z);

    ScriptVector subtract(ScriptVector other);

    ScriptVector subtract(double x, double y, double z);

    ScriptVector multiply(double scalar);

    ScriptVector multiply(ScriptVector other);

    ScriptVector divide(double scalar);

    ScriptVector divide(ScriptVector other);

    // Скалярная произведение
    double dotProduct(ScriptVector other);

    // Векторное произведение
    ScriptVector crossProduct(ScriptVector other);

    double length();

    double distance(ScriptVector other);

    double distance(double x, double y, double z);

    ScriptVector normalize();

    boolean equals(double x, double y, double z);

    boolean equals(ScriptVector other);

    double angleBetween(ScriptVector other);

    boolean isZero();

    BlockPos toBlockPos();
}
