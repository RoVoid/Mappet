package mchorse.mappet.client;

import mchorse.mappet.CommonProxy;
import mchorse.mappet.Mappet;
import mchorse.mappet.MappetConfig;
import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.api.hotkeys.HotkeyState;
import mchorse.mappet.api.scripts.Script;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.hotkey.GuiClientHotkeyScreen;
import mchorse.mappet.client.gui.scripts.scriptedItem.GuiScriptedItemScreen;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.hotkey.PacketTriggeredHotkeys;
import mchorse.mappet.utils.NBTToJsonLike;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.*;

@SideOnly(Side.CLIENT)
public class KeyboardHandler {
    public static final Map<String, Hotkey> hotkeys = new HashMap<>();
    public static boolean hotkeysNeedLoad = true;

    public KeyBinding openMappetDashboard;
    public KeyBinding openHotkeysMenu;
    public KeyBinding runCurrentScript;

    private final KeyBinding openScriptedItem;

    public KeyboardHandler() {
        String prefix = "mappet.keys.";

        openMappetDashboard = new KeyBinding(prefix + "dashboard", Keyboard.KEY_GRAVE, prefix + "category");
        openHotkeysMenu = new KeyBinding(prefix + "hotkeys", Keyboard.KEY_NONE, prefix + "category");
        runCurrentScript = new KeyBinding(prefix + "run_current_script", Keyboard.KEY_F6, prefix + "category");
        openScriptedItem = new KeyBinding(prefix + "scripted_item", Keyboard.KEY_NONE, prefix + "category");

        ClientRegistry.registerKeyBinding(openMappetDashboard);
        ClientRegistry.registerKeyBinding(openHotkeysMenu);
        ClientRegistry.registerKeyBinding(runCurrentScript);
        ClientRegistry.registerKeyBinding(openScriptedItem);
    }

    @SubscribeEvent
    public void onKeyPress(KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (openMappetDashboard.isPressed() && OpHelper.isPlayerOp()) {
            if (MappetConfig.dashboardOnlyCreative.get()) {
                if (mc.player.capabilities.isCreativeMode) {
                    mc.displayGuiScreen(GuiMappetDashboard.get(mc));
                }
            }
            else {
                mc.displayGuiScreen(GuiMappetDashboard.get(mc));
            }
        }
        if (openHotkeysMenu.isPressed()) {
            mc.displayGuiScreen(new GuiClientHotkeyScreen(mc));
        }
        if (runCurrentScript.isPressed()) {
            Script script = GuiMappetDashboard.get(mc).script.getData();
            if (script == null) return;
            mc.player.sendChatMessage("/mp script exec " + mc.player.getName() + " " + script.getId());
        }
        if (openScriptedItem.isPressed()) {
            ItemStack stack = mc.player.getHeldItemMainhand();
            if (!stack.getItem().equals(Items.AIR)) {
                mc.displayGuiScreen(new GuiScriptedItemScreen(mc, stack));
            }
        }

        handleHotkeys();
    }

    private void handleHotkeys() {
        int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();
        Set<HotkeyState> hotkeyStates = new HashSet<>();
        for (Hotkey hotkey : hotkeys.values()) {
            if (hotkey.keycode == -1 && hotkey.defaultKeycode != key) continue;
            if (hotkey.keycode != -1 && hotkey.keycode != key) continue;
            if (state && hotkey.mode == Hotkey.Mode.UP) continue;
            if (!state && hotkey.mode == Hotkey.Mode.DOWN) continue;
            hotkeyStates.add(HotkeyState.of(hotkey.name, state));
        }
        if (!hotkeyStates.isEmpty()) Dispatcher.sendToServer(new PacketTriggeredHotkeys(hotkeyStates));
    }

    public static void loadClientKeys(List<Hotkey> hotkeys) {
        try {
            if (hotkeysNeedLoad) {
                hotkeysNeedLoad = false;
                if (CommonProxy.configFolder == null) return;
                File keybinds = new File(CommonProxy.configFolder, "keybinds.json");
                if (!keybinds.isFile()) return;

                NBTTagCompound keysNbt = NBTToJsonLike.read(keybinds);

                for (Hotkey hotkey : hotkeys)
                    if (keysNbt.hasKey(hotkey.name)) hotkey.keycode = keysNbt.getInteger(hotkey.name);
            }
            else for (Hotkey hotkey : hotkeys) {
                hotkey.keycode = KeyboardHandler.hotkeys.getOrDefault(hotkey.name, hotkey).keycode;
            }
        } catch (Exception e) {
            Mappet.logger.error("Failed to load keybinds from file: " + e.getMessage());
        }
    }

    public static void saveClientKeys() {
        try {
            System.out.println("PreSave");

            if (CommonProxy.configFolder == null) return;
            File keybinds = new File(CommonProxy.configFolder, "keybinds.json");

            NBTTagCompound keysNbt;
            if (keybinds.isFile()) keysNbt = NBTToJsonLike.read(keybinds);
            else keysNbt = new NBTTagCompound();

            for (Hotkey hotkey : hotkeys.values()) {
                if (hotkey.keycode == -1) keysNbt.removeTag(hotkey.name);
                else keysNbt.setInteger(hotkey.name, hotkey.keycode);
            }

            System.out.println("Save: " + keysNbt);

            NBTToJsonLike.write(keybinds, keysNbt);
        } catch (Exception e) {
            Mappet.logger.error("Failed to save keybinds from file: " + e.getMessage());
        }
    }
}