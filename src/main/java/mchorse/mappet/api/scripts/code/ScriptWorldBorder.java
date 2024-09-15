package mchorse.mappet.api.scripts.code;

import mchorse.mappet.api.scripts.user.IScriptWorldBorder;
import net.minecraft.world.border.WorldBorder;

import javax.vecmath.Vector2d;

public class ScriptWorldBorder implements IScriptWorldBorder {
    private final WorldBorder border;

    public ScriptWorldBorder(WorldBorder border) {
        this.border = border;
    }

    @Override
    public WorldBorder getMinecraftWorldBorder() {
        return border;
    }
    @Override
    public String getStatus() {
        return border.getStatus().name();
    }
    @Override
    public Vector2d getCenter() {
        return new Vector2d(border.getCenterX(), border.getCenterZ());
    }
    @Override
    public void setCenter(double x, double z) {
        border.setCenter(x, z);
    }
    @Override
    public int getSize() {
        return border.getSize();
    }
    @Override
    public void setSize(int size) {
        border.setSize(size);
    }
    @Override
    public void setTransition(double size) {
        border.setTransition(size);
    }
    @Override
    public void setTransition(double oldSize, double newSize, long time) {
        border.setTransition(oldSize, newSize, time);
    }
    @Override
    public double getDamageBuffer() {
        return border.getDamageBuffer();
    }
    @Override
    public void setDamageBuffer(double bufferSize) {
        border.setDamageBuffer(bufferSize);
    }
    @Override
    public double getDamageAmount() {
        return border.getDamageAmount();
    }
    @Override
    public void setDamageAmount(double amount) {
        border.setDamageAmount(amount);
    }
    @Override
    public int getWarningTime() {
        return border.getWarningTime();
    }
    @Override
    public void setWarningTime(int time) {
        border.setWarningTime(time);
    }
    @Override
    public int getWarningDistance() {
        return border.getWarningDistance();
    }
    @Override
    public void setWarningDistance(int distance) {
        border.setWarningDistance(distance);
    }
}
