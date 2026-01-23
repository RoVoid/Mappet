package mchorse.mappet.api.scripts.user.entities.player;

import mchorse.mappet.api.scripts.code.entities.player.ScriptCamera;

public interface IScriptCamera {

    Float getPitch();

    Float getRoll();

    Float getRotateX();

    Float getRotateY();

    Float getRotateZ();

    Float getTilt();

    Float getX();

    Float getY();

    ScriptCamera rx(Float x);

    ScriptCamera ry(Float y);

    ScriptCamera rz(Float z);

    Float getYaw();

    Float getZ();

    ScriptCamera pitch(Float pitch);

    ScriptCamera roll(Float roll);

    void takeScreenshot(String name);

    void takeScreenshot(String name, boolean share);

    ScriptCamera tilt(Float tilt);

    ScriptCamera x(Float x);

    ScriptCamera y(Float y);

    ScriptCamera yaw(Float yaw);

    ScriptCamera z(Float z);
}
