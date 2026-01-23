package mchorse.mappet.api.ui;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.scripts.code.ui.UIComponent;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.common.ui.PacketUIData;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UIContext {
    public NBTTagCompound data = new NBTTagCompound();
    public EntityPlayer player;
    public UI ui;

    private String script = "";
    private String function = "";

    @SideOnly(Side.CLIENT)
    private Map<String, GuiElement> elements;

    @SideOnly(Side.CLIENT)
    private Set<String> reservedData;

    private String last = "";
    private boolean closed;
    private String hotkey = "";
    private String context = "";
    private NBTTagCompound mouse = new NBTTagCompound();
    private Long dirty;

    public UIContext(UI ui) {
        this.ui = ui;
    }

    public UIContext(UI ui, EntityPlayer player, String script, String function) {
        this.ui = ui;
        this.player = player;
        this.script = script == null ? "" : script;
        this.function = function == null ? "" : function;
    }

    /* Data sync code */

    public UIComponent getById(String id) {
        return getByIdRecursive(id, ui.root);
    }

    private UIComponent getByIdRecursive(String id, UIComponent component) {
        for (UIComponent child : component.getChildComponents()) {
            if (child.id.equals(id)) return child;

            UIComponent result = getByIdRecursive(id, child);
            if (result != null) return result;
        }

        return null;
    }

    public void clearChanges() {
        clearChangesRecursive(ui.root);
    }

    private void clearChangesRecursive(UIComponent component) {
        for (UIComponent child : component.getChildComponents()) {
            child.clearChanges();
            clearChangesRecursive(child);
        }
    }

    public NBTTagCompound compileChanges() {
        NBTTagCompound tag = new NBTTagCompound();

        compileChangesRecursive(tag, ui.root);

        return tag;
    }

    private void compileChangesRecursive(NBTTagCompound tag, UIComponent component) {
        for (UIComponent child : component.getChildComponents()) {
            if (!child.id.isEmpty()) {
                compileComponent(tag, child);
            }

            compileChangesRecursive(tag, child);
        }
    }

    private void compileComponent(NBTTagCompound tag, UIComponent component) {
        Set<String> changes = component.getChanges();

        if (changes.isEmpty()) return;

        NBTTagCompound full = component.serializeNBT();
        NBTTagCompound partial = new NBTTagCompound();

        for (String key : changes) {
            if (full.hasKey(key)) partial.setTag(key, full.getTag(key));
        }

        tag.setTag(component.id, partial);
    }

    public void populateDefaultData() {
        populateDefaultDataRecursive(ui.root);
    }

    private void populateDefaultDataRecursive(UIComponent component) {
        for (UIComponent child : component.getChildComponents()) {
            child.populateData(data);
            populateDefaultDataRecursive(child);
        }
    }

    /* Getters */

    public String getLast() {
        return last;
    }

    public String getHotkey() {
        return hotkey;
    }

    public String getContext() {
        return context;
    }

    public boolean isClosed() {
        return closed;
    }

    public NBTTagCompound getMouse() {
        return mouse;
    }

    public void setMouse(NBTTagCompound mouse) {
        this.mouse = mouse;
    }

    public boolean isDirty() {
        return dirty != null && System.currentTimeMillis() >= dirty;
    }

    public boolean isDirtyInProgress() {
        return dirty != null;
    }

    /* Client side code */

    @SideOnly(Side.CLIENT)
    public void registerElement(String id, GuiElement element, boolean reserved) {
        if (elements == null) {
            elements = new HashMap<>();
        }

        elements.put(id, element);

        if (reserved) {
            if (reservedData == null) {
                reservedData = new HashSet<>();
            }

            reservedData.add(id);
        }
    }

    @SideOnly(Side.CLIENT)
    public GuiElement getElement(String target) {
        return elements == null ? null : elements.get(target);
    }

    @SideOnly(Side.CLIENT)
    public Set<String> getElementKeys() {
        return elements.keySet();
    }

    @SideOnly(Side.CLIENT)
    public void sendKey(String action) {
        if (dirty != null) {
            sendToServer();
        }
        else {
            NBTTagCompound tag = new NBTTagCompound();

            tag.setString("Hotkey", action);

            Dispatcher.sendToServer(new PacketUIData(tag));
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendContext(String action) {
        if (dirty != null) {
            sendToServer();
        }
        else {
            NBTTagCompound tag = new NBTTagCompound();

            tag.setString("Context", action);

            Dispatcher.sendToServer(new PacketUIData(tag));
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendMouse() {
        if (dirty != null) {
            sendToServer();
        }
        else {
            NBTTagCompound tag = new NBTTagCompound();

            tag.setTag("Mouse", mouse);
            mouse = new NBTTagCompound();

            Dispatcher.sendToServer(new PacketUIData(tag));
        }
    }

    @SideOnly(Side.CLIENT)
    public void dirty(String id, long delay) {
        last = id;

        if (delay <= 0) {
            dirty = null;
            sendToServer();
        }
        else {
            dirty = System.currentTimeMillis() + delay;
        }
    }

    @SideOnly(Side.CLIENT)
    public void sendToServer() {
        dirty = null;

        NBTTagCompound tag = new NBTTagCompound();

        tag.setTag("Data", data);
        tag.setString("Last", last);
        tag.setString("Hotkey", hotkey);
        tag.setString("Context", context);
        tag.setTag("Mouse", mouse);

        NBTTagCompound oldData = data;

        data = new NBTTagCompound();

        if (reservedData != null) {
            for (String key : reservedData) {
                if (oldData.hasKey(key)) data.setTag(key, oldData.getTag(key));
            }
        }

        hotkey = "";
        mouse = new NBTTagCompound();

        Dispatcher.sendToServer(new PacketUIData(tag));
    }

    /* Server side code */

    public void handleNewData(NBTTagCompound data) {
        if (player == null) return;

        this.data.merge(data.getCompoundTag("Data"));
        last = data.getString("Last");
        hotkey = data.getString("Hotkey");
        context = data.getString("Context");
        mouse = data.getCompoundTag("Mouse");

        if (handleScript(player)) sendToPlayer();
        else clearChanges();
    }

    public void sendToPlayer() {
        NBTTagCompound changes = compileChanges();

        if (!changes.getKeySet().isEmpty()) {
            Dispatcher.sendTo(new PacketUIData(changes), (EntityPlayerMP) player);
        }

        clearChanges();
    }

    public void close() {
        if (player == null) return;

        closed = true;
        last = "";

        handleScript(player);
    }

    private boolean handleScript(EntityPlayer player) {
        if (script.isEmpty() || function.isEmpty()) return false;

        try {
            Mappet.scripts.execute(script, function, new DataContext(player));
            return true;
        } catch (Exception e) {
            Mappet.logger.error(e.getMessage());
        }

        return false;
    }
}