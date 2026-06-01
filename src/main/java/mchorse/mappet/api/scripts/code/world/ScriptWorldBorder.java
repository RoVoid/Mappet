package mchorse.mappet.api.scripts.code.world;

import mchorse.mappet.api.scripts.code.math.ScriptVector;
import net.minecraft.world.border.WorldBorder;

public class ScriptWorldBorder {
    private final WorldBorder border;

    public ScriptWorldBorder(WorldBorder border) {
        this.border = border;
    }

    public WorldBorder getMinecraftWorldBorder() {
        return border;
    }

    public String getStatus() {
        return border.getStatus().name();
    }

    public ScriptVector getCenter() {
        return new ScriptVector(border.getCenterX(), 0, border.getCenterZ());
    }

    public void setCenter(double x, double z) {
        border.setCenter(x, z);
    }

    public void setTransition(double size) {
        border.setTransition(size);
    }

    public void setTransition(double size, long time) {
        setTransition(border.getDiameter(), size, time);
    }

    public void setTransition(double oldSize, double newSize, long time) {
        border.setTransition(oldSize, newSize, time * 1000);
    }

    public double getDamageBuffer() {
        return border.getDamageBuffer();
    }

    public void setDamageBuffer(double bufferSize) {
        border.setDamageBuffer(bufferSize);
    }

    public double getDamageAmount() {
        return border.getDamageAmount();
    }

    public void setDamageAmount(double amount) {
        border.setDamageAmount(amount);
    }

    public int getWarningTime() {
        return border.getWarningTime();
    }

    public void setWarningTime(int time) {
        border.setWarningTime(time);
    }

    public int getWarningDistance() {
        return border.getWarningDistance();
    }

    public void setWarningDistance(int distance) {
        border.setWarningDistance(distance);
    }
}
