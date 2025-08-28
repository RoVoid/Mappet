package mchorse.mappet.capabilities.camera;

import mchorse.mappet.entities.EntityCamera;
import net.minecraft.nbt.NBTTagCompound;

public class Camera implements ICamera {
    private EntityCamera camera;
    private String playerName;

    public static Camera get(EntityCamera entityCamera) {
        ICamera cameraCapability = entityCamera == null ? null : entityCamera.getCapability(CameraProvider.CAMERA, null);
        if (cameraCapability instanceof Camera) {
            Camera camera = (Camera) cameraCapability;
            camera.camera = entityCamera;
            return camera;
        }
        return null;
    }

    @Override
    public void setPlayer(String name) {
        playerName = name;
    }

    @Override
    public String getPlayer() {
        return playerName;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (playerName != null) tag.setString("Player", playerName);
        return tag;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Player")) playerName = nbt.getString("Player");
    }
}
