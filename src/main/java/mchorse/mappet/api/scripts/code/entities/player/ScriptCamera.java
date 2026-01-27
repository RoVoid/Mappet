package mchorse.mappet.api.scripts.code.entities.player;

import mchorse.mappet.api.scripts.user.entities.player.IScriptCamera;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.PacketCamera;
import mchorse.mappet.network.packets.PacketScreenshot;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.HashMap;
import java.util.Map;

public class ScriptCamera implements IScriptCamera {
    private final EntityPlayerMP player;

    private static Float yaw;
    private static Float pitch;
    private static Float roll;
    private static Float tilt;
    private static Float x;
    private static Float y;
    private static Float z;
    private static Float rx;
    private static Float ry;
    private static Float rz;

    private final Map<String, Float> request = new HashMap<>();

    public ScriptCamera(EntityPlayerMP player) {
        this.player = player;
    }

    @Override
    public void takeScreenshot(String name) {
        takeScreenshot(name, true);
    }

    @Override
    public void takeScreenshot(String name, boolean share) {
        Dispatcher.sendTo(new PacketScreenshot(name, share), player);
    }

    @Override
    public ScriptCamera yaw(Float yaw) {
        request.put("yaw", yaw);
        return this;
    }

    @Override
    public ScriptCamera pitch(Float pitch) {
        request.put("pitch", pitch);
        return this;
    }

    @Override
    public ScriptCamera roll(Float roll) {
        request.put("roll", roll);
        return this;
    }

    @Override
    public ScriptCamera tilt(Float tilt) {
        request.put("tilt", tilt);
        return this;
    }

    @Override
    public ScriptCamera x(Float x) {
        request.put("x", x);
        return this;
    }

    @Override
    public ScriptCamera y(Float y) {
        request.put("y", y);
        return this;
    }

    @Override
    public ScriptCamera z(Float z) {
        request.put("z", z);
        return this;
    }

    @Override
    public ScriptCamera rx(Float x) {
        request.put("x", x);
        return this;
    }

    @Override
    public ScriptCamera ry(Float y) {
        request.put("y", y);
        return this;
    }

    @Override
    public ScriptCamera rz(Float z) {
        request.put("z", z);
        return this;
    }

    @Override
    public Float getYaw() {
        return yaw;
    }

    @Override
    public Float getPitch() {
        return pitch;
    }

    @Override
    public Float getRoll() {
        return roll;
    }

    @Override
    public Float getTilt() {
        return tilt;
    }

    @Override
    public Float getX() {
        return x;
    }

    @Override
    public Float getY() {
        return y;
    }

    @Override
    public Float getZ() {
        return z;
    }

    @Override
    public Float getRotateX() {
        return rx;
    }

    @Override
    public Float getRotateY() {
        return ry;
    }

    @Override
    public Float getRotateZ() {
        return rz;
    }

    public void update() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList ignores = new NBTTagList();
        for (Map.Entry<String, Float> entry : request.entrySet()) {
            if (entry.getValue() == null) ignores.appendTag(new NBTTagString(entry.getKey()));
            else tag.setFloat(entry.getKey(), entry.getValue());
        }
        tag.setTag("ignores", ignores);
        Dispatcher.sendTo(new PacketCamera(tag), player);
        request.clear();
    }
}
