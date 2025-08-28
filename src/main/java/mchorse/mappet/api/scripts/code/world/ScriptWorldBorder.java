package mchorse.mappet.api.scripts.code.world;

import mchorse.mappet.api.scripts.code.math.ScriptVector;
import mchorse.mappet.api.scripts.user.world.IScriptWorldBorder;
import net.minecraft.world.border.WorldBorder;

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
    public ScriptVector getCenter() {
        return new ScriptVector(border.getCenterX(), 0, border.getCenterZ());
    }

    @Override
    public void setCenter(double x, double z) {
        border.setCenter(x, z);
    }

    @Override
    public void setTransition(double size) {
        border.setTransition(size);
    }

    @Override
    public void setTransition(double size, long time) {
        setTransition(border.getDiameter(), size, time);
    }

    @Override
    public void setTransition(double oldSize, double newSize, long time) {
        border.setTransition(oldSize, newSize, time * 1000);
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
