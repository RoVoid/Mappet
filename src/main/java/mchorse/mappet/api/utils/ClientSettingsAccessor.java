package mchorse.mappet.api.utils;

import mchorse.mappet.MappetConfig;
import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraftforge.common.util.Constants.NBT.*;

public class ClientSettingsAccessor {
    private final GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
    private final Map<String, KeyBinding> keyBindCache = new HashMap<>();

    public ClientSettingsAccessor() {
        if (gameSettings == null) return;
        for (KeyBinding kb : gameSettings.keyBindings) {
            keyBindCache.put(kb.getKeyDescription(), kb);
        }
    }

    public NBTTagCompound parse(List<String> requests, NBTTagCompound options) {
        if (gameSettings == null) return new NBTTagCompound();
        NBTTagCompound tag = new NBTTagCompound();
        capture(tag, requests);
        tag.setTag("applied", apply(options));
        return tag;
    }

    public void capture(NBTTagCompound tag, List<String> requests) {
        if (requests == null || requests.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        for (String option : requests) {
            switch (option) {
                case "fov":
                    tag.setFloat("fov", gameSettings.fovSetting);
                    break;
                case "language":
                    tag.setString("language", gameSettings.language);
                    break;
                case "screenSize":
                    tag.setInteger("screenWidth", mc.displayWidth);
                    tag.setInteger("screenHeight", mc.displayHeight);
                    break;
                case "mainHand":
                    tag.setBoolean("mainHand", gameSettings.mainHand == EnumHandSide.RIGHT);
                    break;
                case "showSubtitles":
                    tag.setBoolean("showSubtitles", gameSettings.showSubtitles);
                    break;
                case "gamma":
                    tag.setFloat("gamma", gameSettings.gammaSetting);
                    break;
                case "mouseSensitivity":
                    tag.setFloat("mouseSensitivity", gameSettings.mouseSensitivity);
                    break;
                case "vsync":
                    tag.setBoolean("vsync", gameSettings.enableVsync);
                    break;
                case "renderDistance":
                    tag.setInteger("renderDistance", gameSettings.renderDistanceChunks);
                    break;
                case "chatVisibility":
                    tag.setInteger("chatVisibility", gameSettings.chatVisibility.getChatVisibility());
                    break;
                case "invertMouse":
                    tag.setBoolean("invertMouse", gameSettings.invertMouse);
                    break;
                default:
                    if (option.startsWith("keybind:")) {
                        String id = option.substring("keybind:".length());
                        KeyBinding binding = keyBindCache.get(id);
                        if (binding != null) tag.setInteger("keybind:" + id, binding.getKeyCode());
                    }
                    else if (option.startsWith("mappetKeybind:")) {
                        String id = option.substring("mappetKeybind:".length());
                        Hotkey hotkey = KeyboardHandler.hotkeys.get(id);
                        if (hotkey != null) tag.setInteger("mappetKeybind:" + id, hotkey.keycode);
                    }
                    break;
            }
        }
    }

    public NBTTagCompound apply(NBTTagCompound options) {
        NBTTagCompound result = new NBTTagCompound();
        boolean allApplied = true;
        boolean keybindUpdate = false;
        boolean mappetKeybindUpdate = false;

        boolean deny = MappetConfig.denyClientSettingChanges.get();
        if (deny || options == null || options.hasNoTags()) {
            result.setBoolean(deny ? "deny" : "all", deny);
            return result;
        }

        for (String option : options.getKeySet()) {
            boolean applied = false;
            if (option.equals("fov")) {
                if (options.hasKey("fov", TAG_FLOAT)) {
                    gameSettings.fovSetting = Math.min(110, Math.max(0, options.getFloat("fov")));
                    applied = true;
                }
            }
            else if (option.equals("showSubtitles")) {
                if (options.hasKey("showSubtitles", TAG_BYTE)) {
                    gameSettings.showSubtitles = options.getBoolean("showSubtitles");
                    applied = true;
                }
            }
            else if (option.equals("gamma")) {
                if (options.hasKey("gamma", TAG_FLOAT)) {
                    gameSettings.gammaSetting = Math.max(0, options.getFloat("gamma"));
                    applied = true;
                }
            }
            else if (option.equals("renderDistance")) {
                if (options.hasKey("renderDistance", TAG_INT)) {
                    gameSettings.renderDistanceChunks = Math.max(0, Math.min(32, options.getInteger("renderDistance")));
                    applied = true;
                }
            }
            else if (option.startsWith("keybind:")) {
                String id = option.substring("keybind:".length());
                if (!id.isEmpty() && options.hasKey("keybind:" + id, TAG_INT)) {
                    KeyBinding binding = keyBindCache.get(id);
                    if (binding != null) {
                        binding.setKeyCode(options.getInteger("keybind:" + id));
                        keybindUpdate = true;
                        applied = true;
                    }
                    else {
                        allApplied = false;
                        continue;
                    }
                }

            }
            else if (option.startsWith("mappetKeybind:")) {
                String id = option.substring("mappetKeybind:".length());
                if (!id.isEmpty() && options.hasKey("mappetKeybind:" + id, TAG_INT)) {
                    Hotkey hotkey = KeyboardHandler.hotkeys.get(id);
                    if (hotkey != null) {
                        hotkey.keycode = options.getInteger("mappetKeybind:" + id);
                        mappetKeybindUpdate = true;
                        applied = true;
                    }
                    else {
                        allApplied = false;
                        continue;
                    }
                }
            }
            else {
                allApplied = false;
                continue;
            }

            result.setBoolean(option, applied);
            if (!applied) allApplied = false;
        }

        if (keybindUpdate) KeyBinding.resetKeyBindingArrayAndHash();
        if (mappetKeybindUpdate) KeyboardHandler.saveClientKeys();

        gameSettings.saveOptions();
        result.setBoolean("all", allApplied);
        return result;
    }
}
