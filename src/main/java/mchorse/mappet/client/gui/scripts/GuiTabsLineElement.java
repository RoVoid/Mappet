package mchorse.mappet.client.gui.scripts;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.utils.ScrollDirection;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GuiTabsLineElement extends GuiScrollElement {
    private final Consumer<String> selectCallback;
    private final BiConsumer<String, Boolean> closeCallback; // Может потом оставлю Consumer

    private final Map<String, Node> tabs = new Object2ObjectOpenHashMap<>();
    private Node firstNode;
    private Node lastNode;
    private Node selectedNode;

    public GuiTabsLineElement(Minecraft mc, Consumer<String> selectCallback, BiConsumer<String, Boolean> closeCallback) {
        super(mc, ScrollDirection.HORIZONTAL);

        this.selectCallback = selectCallback;
        this.closeCallback = closeCallback;

        flex().h(font.FONT_HEIGHT + 10).column(5).scroll();
    }

    public void addTab(String path) {
        addTab(path, true);
    }

    public void addTab(String path, boolean select) {
        if (path == null) return;

        Node existing = tabs.get(path);
        if (existing != null) {
            if (select) selectNode(existing);
            return;
        }

        GuiTabElement tab = new GuiTabElement(mc, path, this::onSelected, this::onClosed);
        tab.flex().relative(this).h(1F).anchorY(0.5f);

        Node node = new Node(tab);

        if (firstNode == null) {
            firstNode = lastNode = node;
            node.prev = node.next = node;
        }
        else {
            node.prev = lastNode;
            node.next = firstNode;
            lastNode.next = node;
            firstNode.prev = node;
            lastNode = node;
        }

        tabs.put(path, node);
        if (selectedNode == null) add(tab);
        else addAfter(selectedNode.tab, tab);

        resize();

        if (select) selectNode(node);
    }

    public void removeTab(String path) {
        if (path == null) return;
        Node node = tabs.remove(path);
        if (node == null) return;

        if (node.next == node) firstNode = lastNode = null;
        else {
            node.prev.next = node.next;
            node.next.prev = node.prev;

            if (firstNode == node) firstNode = node.next;
            if (lastNode == node) lastNode = node.prev;
        }

        nextTab();

        if (selectedNode == node) selectedNode = null;
        else if (selectCallback != null) selectCallback.accept(selectedNode.tab.path);

        node.tab.removeFromParent();
        resize();
    }

    public void edited(String path, boolean state) {
        if (path == null) return;
        Node node = tabs.get(path);
        if (node != null) node.tab.edited(state);
    }

    public int length() {return tabs.size();}

    public String selectedPath() {return selectedNode == null ? null : selectedNode.tab.path;}

    public void selectTab(String path) {selectNode(tabs.get(path));}

    public void nextTab() {
        if (selectedNode == null) return;
        selectNode(selectedNode.next);
    }

    public void prevTab() {
        if (selectedNode == null) return;
        selectNode(selectedNode.prev);
    }

    private void selectNode(Node node) {
        if (node == null || node == selectedNode) return;

        if (selectedNode != null) selectedNode.tab.selected = false;

        selectedNode = node;
        selectedNode.tab.selected = true;

        int x = selectedNode.tab.area.x - area.x - scroll.scroll;
        int ex = x + selectedNode.tab.area.w;
        int sw = scroll.w;
        if (x < 0 || ex > sw) scroll.scrollTo(x); // TODO: Подправить
    }

    private void onSelected(GuiTabElement tab) {
        selectTab(tab.path);
        if (selectCallback != null) selectCallback.accept(tab.path);
    }

    private void onClosed(GuiTabElement tab) {
        String previous = selectedPath();
        removeTab(tab.path);
        if (closeCallback != null) closeCallback.accept(tab.path, tab.path.equals(previous));
    }

    private static class Node {
        Node prev;
        Node next;
        GuiTabElement tab;

        Node(GuiTabElement tab) {this.tab = tab;}
    }
}
