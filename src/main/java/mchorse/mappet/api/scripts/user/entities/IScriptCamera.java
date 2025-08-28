package mchorse.mappet.api.scripts.user.entities;

import mchorse.mappet.api.scripts.code.entities.ScriptEntity;

public interface IScriptCamera {
    void removeCamera();

    void setPosition(double x, double y, double z);

    void setRotation(float yaw, float pitch);

    void switchTo(ScriptEntity<?> entity);

    void switchToCamera();

    void switchToOwner();

    void takeScreenshot(String name);

    void takeScreenshot(String name, boolean share);
}
