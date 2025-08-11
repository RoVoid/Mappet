package mchorse.mappet.api.scripts.code.entities.utils;

import mchorse.mappet.api.scripts.code.entities.ScriptPlayer;
import mchorse.mappet.api.scripts.user.entities.IClientSettings;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.scripts.PacketClientSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientSettings implements IClientSettings {
    private final List<String> requests = new ArrayList<>();
    private final NBTTagCompound options = new NBTTagCompound();
    private final Set<String> usedKeys = new HashSet<>();

    private EntityPlayerMP player = null;

    public ClientSettings() {
    }

    public ClientSettings(EntityPlayerMP player) {
        this.player = player;
    }

    private void request(String key) {
        if (usedKeys.add(key)) requests.add(key);
    }

    private void apply(String key, Runnable writer) {
        if (usedKeys.add(key)) writer.run();
    }

    @Override
    public IClientSettings chatVisibility() {
        request("chatVisibility");
        return this;
    }

    @Override
    public IClientSettings chatVisibility(int mode) {
        apply("chatVisibility", () -> options.setInteger("chatVisibility", mode));
        return this;
    }

    @Override
    public IClientSettings fov() {
        request("fov");
        return this;
    }

    @Override
    public IClientSettings fov(float value) {
        apply("fov", () -> options.setFloat("fov", value));
        return this;
    }

    @Override
    public IClientSettings gamma() {
        request("gamma");
        return this;
    }

    @Override
    public IClientSettings gamma(float value) {
        apply("gamma", () -> options.setFloat("gamma", value));
        return this;
    }

    @Override
    public IClientSettings invertMouse() {
        request("invertMouse");
        return this;
    }

    @Override
    public IClientSettings keybind(String id) {
        request("keybind:" + id);
        return this;
    }

    @Override
    public IClientSettings keybind(String id, int code) {
        apply("keybind:" + id, () -> options.setInteger("keybind:" + id, code));
        return this;
    }

    @Override
    public IClientSettings mappetKeybind(String id) {
        request("mappetKeybind:" + id);
        return this;
    }

    @Override
    public IClientSettings mappetKeybind(String id, int code) {
        apply("mappetKeybind:" + id, () -> options.setInteger("mappetKeybind:" + id, code));
        return this;
    }

    @Override
    public IClientSettings language() {
        request("language");
        return this;
    }

    @Override
    public IClientSettings mainHand() {
        request("mainHand");
        return this;
    }

    @Override
    public IClientSettings mouseSensitivity() {
        request("mouseSensitivity");
        return this;
    }

    @Override
    public IClientSettings renderDistance() {
        request("renderDistance");
        return this;
    }

    @Override
    public IClientSettings renderDistance(int chunks) {
        apply("renderDistance", () -> options.setInteger("renderDistance", chunks));
        return this;
    }

    @Override
    public IClientSettings screenSize() {
        request("screenSize");
        return this;
    }

    @Override
    public IClientSettings showSubtitles() {
        request("showSubtitles");
        return this;
    }

    @Override
    public IClientSettings showSubtitles(boolean value) {
        apply("showSubtitles", () -> options.setBoolean("showSubtitles", value));
        return this;
    }

    @Override
    public IClientSettings vsync() {
        request("vsync");
        return this;
    }

    @Override
    public void sendTo(ScriptPlayer... players) {
        sendTo("", "", players);
    }

    @Override
    public void sendTo(String script, ScriptPlayer... players) {
        sendTo(script, "handler", players);
    }

    @Override
    public void sendTo(String script, String function, ScriptPlayer... players) {
        if (requests.isEmpty() && options.hasNoTags()) return;

        PacketClientSettings request = new PacketClientSettings(requests, options, script, function);

        if (player != null) Dispatcher.sendTo(request, player);
        if (players != null) {
            for (ScriptPlayer p : players) Dispatcher.sendTo(request, p.asMinecraft());
        }
    }
}
