package mchorse.mappet.client;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.misc.hotkeys.TriggerHotkey;
import mchorse.mappet.api.scripts.Script;
import mchorse.mappet.client.gui.GuiJournalScreen;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.scripts.scriptedItem.GuiScriptedItemScreen;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.events.PacketEventHotkey;
import mchorse.mappet.network.common.events.PacketPlayerJournal;
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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.*;

/**
 * Keyboard handler
 * <p>
 * This class is responsible for handling keyboard input (i.e. key
 * presses) and storing keybindings associated with this mod.
 */
@SideOnly(Side.CLIENT)
public class KeyboardHandler {
    public static final Set<TriggerHotkey> hotkeys = new HashSet<>();
    public static final List<TriggerHotkey> held = new ArrayList<>();
    public static boolean clientPlayerJournal;

    public KeyBinding openMappetDashboard;
    public KeyBinding openJournal;
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

    public static void updateHeldKeys() {
        if (held.isEmpty()) return;

        Iterator<TriggerHotkey> it = held.iterator();

        while (it.hasNext()) {
            int keycode = it.next().keycode;

            if (!Keyboard.isKeyDown(keycode)) {
                it.remove();

                Dispatcher.sendToServer(new PacketEventHotkey(keycode, false));
            }
        }
    }

    public KeyboardHandler() {
        String prefix = "mappet.keys.";

        openMappetDashboard = new KeyBinding(prefix + "dashboard", Keyboard.KEY_GRAVE, prefix + "category");
        openJournal = new KeyBinding(prefix + "journal", Keyboard.KEY_NONE, prefix + "category");
        runCurrentScript = new KeyBinding(prefix + "runCurrentScript", Keyboard.KEY_F6, prefix + "category");
        openScriptedItem = new KeyBinding(prefix + "scripted_item", Keyboard.KEY_NONE, prefix + "category");

        ClientRegistry.registerKeyBinding(openMappetDashboard);
        ClientRegistry.registerKeyBinding(openJournal);
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
        if (Keyboard.getEventKeyState()) handleKeys();
    }

    private void handleKeys() {
        int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

        for (TriggerHotkey hotkey : hotkeys) {
            if (hotkey.keycode == key) {
                Dispatcher.sendToServer(new PacketEventHotkey(key, true));
                if (hotkey.toggle) held.add(hotkey);
                return;
            }
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