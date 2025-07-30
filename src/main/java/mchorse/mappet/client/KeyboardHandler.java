package mchorse.mappet.client;

import mchorse.mappet.CommonProxy;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.api.hotkeys.HotkeyState;
import mchorse.mappet.api.scripts.Script;
import mchorse.mappet.client.gui.GuiJournalScreen;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.hotkey.GuiClientHotkeyScreen;
import mchorse.mappet.client.gui.scripts.scriptedItem.GuiScriptedItemScreen;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.events.PacketPlayerJournal;
import mchorse.mappet.network.common.events.PacketTriggeredHotkeys;
import mchorse.mappet.utils.NBTToJsonLike;
import mchorse.mclib.client.gui.framework.tooltips.styles.TooltipStyle;
import mchorse.mclib.client.gui.utils.Area;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
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

    public static boolean clientPlayerJournal;

    public KeyBinding openMappetDashboard;
    public KeyBinding openJournal;
    public KeyBinding openHotkeysMenu;
    public KeyBinding runCurrentScript;

    private final KeyBinding openScriptedItem;

    public static void openPlayerJournal() {
        if (clientPlayerJournal) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.displayGuiScreen(new GuiJournalScreen(mc));
        } else {
            Dispatcher.sendToServer(new PacketPlayerJournal());
        }
    }

    public KeyboardHandler() {
        String prefix = "mappet.keys.";

        openMappetDashboard = new KeyBinding(prefix + "dashboard", Keyboard.KEY_GRAVE, prefix + "category");
        openJournal = new KeyBinding(prefix + "journal", Keyboard.KEY_NONE, prefix + "category");
        openHotkeysMenu = new KeyBinding(prefix + "hotkeys", Keyboard.KEY_NONE, prefix + "category");
        runCurrentScript = new KeyBinding(prefix + "run_current_script", Keyboard.KEY_F6, prefix + "category");
        openScriptedItem = new KeyBinding(prefix + "scripted_item", Keyboard.KEY_NONE, prefix + "category");

        ClientRegistry.registerKeyBinding(openMappetDashboard);
        ClientRegistry.registerKeyBinding(openJournal);
        ClientRegistry.registerKeyBinding(openHotkeysMenu);
        ClientRegistry.registerKeyBinding(runCurrentScript);
        ClientRegistry.registerKeyBinding(openScriptedItem);
    }

    @SubscribeEvent
    public void onKeyPress(KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (openMappetDashboard.isPressed() && OpHelper.isPlayerOp()) {
            if (Mappet.dashboardOnlyCreative.get()) {
                if (mc.player.capabilities.isCreativeMode) {
                    mc.displayGuiScreen(GuiMappetDashboard.get(mc));
                }
            } else {
                mc.displayGuiScreen(GuiMappetDashboard.get(mc));
            }
        }
        if (openJournal.isPressed()) {
            openPlayerJournal();
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

    public static void loadClientKeys(Map<String, Integer> clientKeys, List<Hotkey> hotkeys) {
        try {
            hotkeysNeedLoad = false;
            if (CommonProxy.configFolder == null) return;
            File keybinds = new File(CommonProxy.configFolder, "keybinds.json");
            if (!keybinds.isFile()) return;

            NBTTagCompound keysNbt = NBTToJsonLike.read(keybinds);

            for (Hotkey hotkey : hotkeys)
                if (keysNbt.hasKey(hotkey.name)) clientKeys.put(hotkey.name, keysNbt.getInteger(hotkey.name));
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

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiContainerCreative) {
            int x = Mappet.journalButtonX.get();
            int y = event.getGui().height - 20 - Mappet.journalButtonY.get();

            event.getButtonList().add(new GuiJournalButton(-69420, x, y, ""));
        }
    }

    @SubscribeEvent
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getButton() instanceof GuiJournalButton) openPlayerJournal();
    }

    public static class GuiJournalButton extends GuiButton {
        private static final ResourceLocation ICON = new ResourceLocation("textures/items/book_writable.png");
        private static final IKey TOOLTIP = IKey.lang("mappet.gui.player_journal");

        private final Area area = new Area();

        public GuiJournalButton(int buttonId, int x, int y, String buttonText) {
            super(buttonId, x, y, 20, 20, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            float factor = hovered ? 0.8F : 1F;

            GlStateManager.enableTexture2D();
            GlStateManager.color(factor, factor, factor, 1F);

            mc.renderEngine.bindTexture(ICON);
            Gui.drawModalRectWithCustomSizedTexture(this.x + this.width / 2 - 8, this.y + this.height / 2 - 8, 0, 0, 16, 16, 16, 16);

            if (hovered) {
                String label = TOOLTIP.get();
                TooltipStyle style = TooltipStyle.get();
                int w = mc.fontRenderer.getStringWidth(label);

                this.area.set(this.x + 20, this.y + this.height / 2 - 7, w + 6, 14);
                style.drawBackground(this.area);
                mc.fontRenderer.drawString(label, this.area.x + 3, this.area.y + 3, style.getTextColor());
            }
        }
    }
}