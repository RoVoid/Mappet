package mchorse.mappet.api.scripts.user.world;

import net.minecraft.world.border.WorldBorder;

import javax.vecmath.Vector2d;

public interface IScriptWorldBorder {
    WorldBorder getMinecraftWorldBorder();

    String getStatus();

    Vector2d getCenter();

    void setCenter(double x, double z);

    void setTransition(double size);

    /**
     * @param time seconds
     */
    void setTransition(double fromSize, double toSize, long time);

    /**
     * @param time seconds
     */
    void setTransition(double size, long time);

    double getDamageBuffer();

    void setDamageBuffer(double bufferSize);

    double getDamageAmount();

    void setDamageAmount(double amount);

    int getWarningTime();

    void setWarningTime(int time);

    int getWarningDistance();

    void setWarningDistance(int distance);
}
