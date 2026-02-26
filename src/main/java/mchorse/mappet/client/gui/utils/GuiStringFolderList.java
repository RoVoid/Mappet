package mchorse.mappet.client.gui.utils;

import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.Icons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.*;
import java.util.function.Consumer;

public class GuiStringFolderList extends GuiStringListElement {

    private final Set<String> hierarchy = new HashSet<>();
    private String path = "";
    private Icon fileIcon = Icons.FILE;

    public GuiStringFolderList(Minecraft mc, Consumer<List<String>> callback) {
        super(mc, null);
        this.callback = l -> handleClick(callback, l);
    }

    private void handleClick(Consumer<List<String>> callback, List<String> list) {
        String entry = list.get(0);

        if (entry.endsWith("/")) {
            goTo(entry.endsWith("../") ? parent(path) : resolve(entry.substring(0, entry.length() - 1)));
            return;
        }

        list.clear();
        list.add(resolve(entry));
        callback.accept(list);
    }

    public void setFileIcon(Icon icon) {
        fileIcon = icon;
    }

    public String getPath() {
        return path;
    }

    public String getPath(String name) {
        return resolve(name);
    }

    public void fill(Collection<String> files) {
        hierarchy.clear();
        hierarchy.addAll(files);
        goTo("");
    }

    public void root() {
        goTo("");
    }

    public void up() {
        if (!path.isEmpty()) goTo(parent(path));
    }

    public void down(String folder) {
        goTo(resolve(folder));
    }

    private void goTo(String path) {
        this.path = path;
        filter("");
        setIndex(-1);
        rebuild();
    }

    private void rebuild() {
        list.clear();

        if (!path.isEmpty()) list.add(getPath("../"));

        Set<String> entries = new HashSet<>();
        int baseLen = path.length();

        for (String p : hierarchy) {
            if (!path.isEmpty() && !p.startsWith(path + "/")) continue;

            String rest = path.isEmpty() ? p : p.substring(baseLen + 1);
            if (rest.isEmpty()) continue;

            int i = rest.indexOf('/');
            entries.add(i < 0 ? rest : rest.substring(0, i + 1));
        }

        list.addAll(entries);
        sort();
        update();
    }

    public boolean notInHierarchy(String path) {
        return !hierarchy.contains(path);
    }

    public void addFile(String path) {
        if (hierarchy.add(path)) {
            String name = filename(path);
            if (name != null) {
                add(name);
                sort();
                setCurrentFile(path);
            }
        }
    }

    public void removeFile(String path) {
        if (hierarchy.remove(path)) {
            String name = filename(path);
            if (name != null) {
                remove(name);
                setIndex(-1);
            }
        }
    }

    public String filename(String fullPath) {
        if (!fullPath.startsWith(path)) return null;

        String rest = path.isEmpty() ? fullPath : fullPath.substring(path.length() + 1);

        return rest.indexOf('/') < 0 ? rest : null;
    }

    public void setCurrentFile(String path) {
        if (path == null) return;

        int i = path.lastIndexOf('/');
        goTo(i < 0 ? "" : path.substring(0, i));
        setCurrentScroll(i < 0 ? path : path.substring(i + 1));
    }

    public void clearCurrentFile(){
        setCurrent("");
    }

    private static String parent(String path) {
        int i = path.lastIndexOf('/');
        return i < 0 ? "" : path.substring(0, i);
    }

    private String resolve(String name) {
        return path.isEmpty() ? name : path + "/" + name;
    }

    @Override
    protected void drawElementPart(String element, int i, int x, int y, boolean hover, boolean selected) {
        GlStateManager.color(1, 1, 1, 1);
        (element.endsWith("/") ? element.endsWith("../") ? Icons.LEFTLOAD : Icons.FOLDER : fileIcon).render(x, y);
        super.drawElementPart(element, i, x + 12, y, hover, selected);
    }

    @Override
    protected boolean sortElements() {
        list.sort(Comparator.comparingInt((String s) -> s.endsWith("../") ? 0 : s.endsWith("/") ? 1 : 2).thenComparing(String::compareTo));
        return true;
    }
}
