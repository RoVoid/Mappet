package mchorse.mappet.api.scripts.code.entities.utils;

import mchorse.mappet.api.scripts.code.entities.ScriptEntity;
import mchorse.mappet.api.scripts.user.entities.IScriptCamera;
import mchorse.mappet.entities.EntityCamera;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.PacketCamera;
import mchorse.mappet.network.common.PacketScreenshot;
import net.minecraft.entity.player.EntityPlayerMP;

public class ScriptCamera implements IScriptCamera {

    private EntityCamera camera;
    private final EntityPlayerMP owner;

    public ScriptCamera(EntityPlayerMP player) {
        owner = player;
    }

    private void prepareCamera() {
        if (camera != null) return;
        camera = new EntityCamera(owner);
        camera.setPosition(owner.posX, owner.posY + owner.getEyeHeight(), owner.posZ);
        owner.world.spawnEntity(camera);
    }

    @Override
    public void switchToCamera() {
        prepareCamera();
        Dispatcher.sendTo(new PacketCamera(camera), owner);
    }

    @Override
    public void switchToOwner() {
        prepareCamera();
        Dispatcher.sendTo(new PacketCamera(owner), owner);
    }

    @Override
    public void switchTo(ScriptEntity<?> entity) {
        prepareCamera();
        Dispatcher.sendTo(new PacketCamera(entity.asMinecraft()), owner);
    }

    @Override
    public void setPosition(double x, double y, double z) {
        if (camera == null) return;
        camera.posX = x;
        camera.posY = y;
        camera.posZ = z;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        if (camera == null) return;
        camera.rotationYaw = yaw;
        camera.rotationPitch = pitch;
    }

    @Override
    public void removeCamera() {
        if (camera != null) {
            camera.setDead();
            camera.world.removeEntity(camera);
            camera = null;
        }
        Dispatcher.sendTo(new PacketCamera(owner), owner);
    }

    @Override
    public void takeScreenshot(String name) {
        takeScreenshot(name, true);
    }

    @Override
    public void takeScreenshot(String name, boolean share) {
        Dispatcher.sendTo(new PacketScreenshot(name, share), owner);
    }
}
