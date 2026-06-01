package mchorse.mappet.api.scripts.code.entities.player;

import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.PacketCamera;
import mchorse.mappet.network.packets.PacketScreenshot;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.HashMap;
import java.util.Map;

public class ScriptCamera {
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

    public void takeScreenshot(String name) {
        takeScreenshot(name, true);
    }

    public void takeScreenshot(String name, boolean share) {
        Dispatcher.sendTo(new PacketScreenshot(name, share), player);
    }

    public ScriptCamera yaw(Float yaw) {
        request.put("yaw", yaw);
        return this;
    }

    public ScriptCamera pitch(Float pitch) {
        request.put("pitch", pitch);
        return this;
    }

    public ScriptCamera roll(Float roll) {
        request.put("roll", roll);
        return this;
    }

    public ScriptCamera tilt(Float tilt) {
        request.put("tilt", tilt);
        return this;
    }

    public ScriptCamera x(Float x) {
        request.put("x", x);
        return this;
    }

    public ScriptCamera y(Float y) {
        request.put("y", y);
        return this;
    }

    public ScriptCamera z(Float z) {
        request.put("z", z);
        return this;
    }

    public ScriptCamera rx(Float x) {
        request.put("x", x);
        return this;
    }

    public ScriptCamera ry(Float y) {
        request.put("y", y);
        return this;
    }

    public ScriptCamera rz(Float z) {
        request.put("z", z);
        return this;
    }

    public Float getYaw() {
        return yaw;
    }

    public Float getPitch() {
        return pitch;
    }

    public Float getRoll() {
        return roll;
    }

    public Float getTilt() {
        return tilt;
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    public Float getZ() {
        return z;
    }

    public Float getRotateX() {
        return rx;
    }

    public Float getRotateY() {
        return ry;
    }

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
