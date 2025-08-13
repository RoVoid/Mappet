package mchorse.mappet.api.scripts.user.entities;

import mchorse.mappet.api.scripts.code.entities.ScriptPlayer;

public interface IClientSettings {
    IClientSettings chatVisibility();
    IClientSettings chatVisibility(int mode);

    IClientSettings fov();
    IClientSettings fov(float value);

    IClientSettings gamma();
    IClientSettings gamma(float value);

    IClientSettings invertMouse();

    IClientSettings keybind(String id);
    IClientSettings keybind(String id, int code);

    IClientSettings keybinds();

    IClientSettings mappetKeybind(String id);

    IClientSettings mappetKeybind(String id, int code);

    IClientSettings language();

    IClientSettings mainHand();

    IClientSettings mouseSensitivity();

    IClientSettings renderDistance();
    IClientSettings renderDistance(int chunks);

    IClientSettings screenSize();

    void sendTo(ScriptPlayer... players);
    void sendTo(String script, ScriptPlayer... players);
    void sendTo(String script, String function, ScriptPlayer... players);

    IClientSettings showSubtitles();
    IClientSettings showSubtitles(boolean value);

    IClientSettings vsync();
}
