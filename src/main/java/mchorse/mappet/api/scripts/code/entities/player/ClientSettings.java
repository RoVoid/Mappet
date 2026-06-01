package mchorse.mappet.api.scripts.code.entities.player;

import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.scripts.PacketClientSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientSettings {
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

    public ClientSettings chatVisibility() {
        request("chatVisibility");
        return this;
    }

    public ClientSettings chatVisibility(int mode) {
        apply("chatVisibility", () -> options.setInteger("chatVisibility", mode));
        return this;
    }

    public ClientSettings fov() {
        request("fov");
        return this;
    }

    public ClientSettings fov(float value) {
        apply("fov", () -> options.setFloat("fov", value));
        return this;
    }

    public ClientSettings gamma() {
        request("gamma");
        return this;
    }

    public ClientSettings gamma(float value) {
        apply("gamma", () -> options.setFloat("gamma", value));
        return this;
    }

    public ClientSettings invertMouse() {
        request("invertMouse");
        return this;
    }

    public ClientSettings keybind(String id) {
        request("keybind:" + id);
        return this;
    }

    public ClientSettings keybind(String id, int code) {
        apply("keybind:" + id, () -> options.setInteger("keybind:" + id, code));
        return this;
    }

    public ClientSettings keybinds() {
        request("keybinds");
        return this;
    }

    public ClientSettings mappetKeybind(String id) {
        request("mappetKeybind:" + id);
        return this;
    }

    public ClientSettings mappetKeybind(String id, int code) {
        apply("mappetKeybind:" + id, () -> options.setInteger("mappetKeybind:" + id, code));
        return this;
    }

    public ClientSettings language() {
        request("language");
        return this;
    }

    public ClientSettings mainHand() {
        request("mainHand");
        return this;
    }

    public ClientSettings mouseSensitivity() {
        request("mouseSensitivity");
        return this;
    }

    public ClientSettings renderDistance() {
        request("renderDistance");
        return this;
    }

    public ClientSettings renderDistance(int chunks) {
        apply("renderDistance", () -> options.setInteger("renderDistance", chunks));
        return this;
    }

    public ClientSettings screenSize() {
        request("screenSize");
        return this;
    }

    public ClientSettings showSubtitles() {
        request("showSubtitles");
        return this;
    }

    public ClientSettings showSubtitles(boolean value) {
        apply("showSubtitles", () -> options.setBoolean("showSubtitles", value));
        return this;
    }

    public ClientSettings vsync() {
        request("vsync");
        return this;
    }

    public void sendTo(ScriptPlayer... players) {
        sendTo("", "", players);
    }

    public void sendTo(String script, ScriptPlayer... players) {
        sendTo(script, "handler", players);
    }

    public void sendTo(String script, String function, ScriptPlayer... players) {
        if (requests.isEmpty() && options.hasNoTags()) return;

        PacketClientSettings request = new PacketClientSettings(requests, options, script, function);

        if (player != null) Dispatcher.sendTo(request, player);
        if (players != null) {
            for (ScriptPlayer p : players) Dispatcher.sendTo(request, p.asMinecraft());
        }
    }
}
