package mchorse.mappet.api.scripts.user;

import net.minecraft.world.border.WorldBorder;

import javax.vecmath.Vector2d;

public interface IScriptWorldBorder {
    WorldBorder getMinecraftWorldBorder();

    String getStatus();

    Vector2d getCenter();

    void setCenter(double x, double z);

    int getSize();

    void setSize(int size);

    void setTransition(double size);

    void setTransition(double oldSize, double newSize, long time);

    double getDamageBuffer();

    void setDamageBuffer(double bufferSize);

    double getDamageAmount();

    void setDamageAmount(double amount);

    int getWarningTime();

    void setWarningTime(int time);

    int getWarningDistance();

    void setWarningDistance(int distance);
}
