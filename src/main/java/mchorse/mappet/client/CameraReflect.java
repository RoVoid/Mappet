package mchorse.mappet.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class CameraReflect {
    static Float yaw;
    static Float pitch;
    static Float roll;
    static Float tilt;
    static Float x;
    static Float y;
    static Float z;
    static Float rx;
    static Float ry;
    static Float rz;

    public static void update(NBTTagCompound tag) {
        Set<String> ignores = new HashSet<>();
        for (NBTBase nbtBase : tag.getTagList("ignores", Constants.NBT.TAG_STRING)) {
            if (nbtBase instanceof NBTTagString) ignores.add(((NBTTagString) nbtBase).getString());
        }

        yaw = read(yaw, "yaw", tag, ignores);
        pitch = read(pitch, "pitch", tag, ignores);
        roll = read(roll, "roll", tag, ignores);
        tilt = read(tilt, "tilt", tag, ignores);
        x = read(x, "x", tag, ignores);
        y = read(y, "y", tag, ignores);
        z = read(z, "z", tag, ignores);
        rx = read(rx, "x", tag, ignores);
        ry = read(ry, "y", tag, ignores);
        rz = read(rz, "z", tag, ignores);
    }

    public static Float read(Float previous, String key, NBTTagCompound tag, Set<String> ignores) {
        if (ignores.contains(key)) return null;
        if (tag.hasKey(key, Constants.NBT.TAG_FLOAT)) return tag.getFloat(key);
        return previous;
    }


    public static void onSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (yaw != null) event.setYaw(yaw);
        if (pitch != null) event.setPitch(pitch);
        if (roll != null) event.setRoll(roll);

        if (tilt != null) GlStateManager.rotate(tilt, 1F, 0F, 0F);

        float dx = x != null ? x : 0F;
        float dy = y != null ? y : 0F;
        float dz = z != null ? z : 0F;
        GlStateManager.translate(dx, dy, dz);

        float drx = rx != null ? rx : 0F;
        float dry = ry != null ? ry : 0F;
        float drz = rz != null ? rz : 0F;
        GlStateManager.rotate(0, drx, dry, drz);
    }
}
