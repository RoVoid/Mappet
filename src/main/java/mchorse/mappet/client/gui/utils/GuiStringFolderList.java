package mchorse.mappet.client.gui.utils;

import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.Icons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GuiStringFolderList extends GuiStringListElement {

    private final Set<String> hierarchy = new HashSet<>();
    private String folderPath = "";
    private Icon fileIcon = Icons.FILE;

    public GuiStringFolderList(Minecraft mc, Consumer<List<String>> callback) {
        super(mc, null);
        this.callback = l -> handleClick(callback, l);
    }

    private void handleClick(Consumer<List<String>> callback, List<String> list) {
        String entry = list.get(0);

        if (entry.endsWith("/")) {
            goTo(entry.endsWith("../") ? parent(folderPath) : getPath(entry.substring(0, entry.length() - 1)));
            return;
        }

        list.clear();
        list.add(getPath(entry));
        callback.accept(list);
    }

    public void setFileIcon(Icon icon) {
        fileIcon = icon;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getPath(String name) {
        return folderPath.isEmpty() ? name : folderPath + "/" + name;
    }

    public void fill(Set<String> files) {
        hierarchy.clear();
        hierarchy.addAll(files);
        root();
    }

    public void root() {
        goTo("");
    }

    public void up() {
        if (!folderPath.isEmpty()) goTo(parent(folderPath));
    }

    public void down(String folderName) {
        goTo(getPath(folderName));
    }

    private void goTo(String path) {
        if (path == null) return;
        folderPath = path;
        filter("");
        setIndex(-1);
        rebuild();
    }

    private void rebuild() {
        list.clear();

        if (!folderPath.isEmpty()) list.add(getPath("../"));

        Set<String> entries = new HashSet<>();
        int baseLen = folderPath.length();

        for (String p : hierarchy) {
            if (!folderPath.isEmpty() && !p.startsWith(folderPath + "/")) continue;

            String rest = folderPath.isEmpty() ? p : p.substring(baseLen + 1);
            if (rest.isEmpty()) continue;

            int i = rest.indexOf('/');
            entries.add(i < 0 ? rest : rest.substring(0, i + 1));
        }

        list.addAll(entries);
        sort();
        update();
    }

    public boolean inHierarchy(String path) {
        return hierarchy.contains(path);
    }

    public boolean folderExists(String folderPath) {
        String prefix = folderPath.endsWith("/") ? folderPath : folderPath + "/";
        return hierarchy.contains(folderPath) || hierarchy.stream().anyMatch(p -> p.startsWith(prefix));
    }

    public void addFile(String path) {
        if (hierarchy.add(path)) {
            rebuild();
            selectFile(path);
        }
    }

    public void removeFile(String path) {
        if (hierarchy.remove(path)) {
            rebuild();
            setIndex(-1);
        }
    }

    public String filename(String fullPath) {
        if (fullPath == null) return null;

        if (folderPath.isEmpty()) return fullPath.indexOf('/') < 0 ? fullPath : null;

        if (!fullPath.startsWith(folderPath + "/")) return null;

        String rest = fullPath.substring(folderPath.length() + 1);
        return rest.indexOf('/') < 0 ? rest : null;
    }

    public void selectFile(String filePath) {
        if (filePath == null) return;

        int i = filePath.lastIndexOf('/');
        goTo(i < 0 ? "" : filePath.substring(0, i));
        setCurrentScroll(i < 0 ? filePath : filePath.substring(i + 1));
    }

    public void unselectFile() {
        setCurrent("");
    }

    private static String parent(String path) {
        if (path.isEmpty()) return null;
        int i = path.lastIndexOf('/');
        return i < 0 ? "" : path.substring(0, i);
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