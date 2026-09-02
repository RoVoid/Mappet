package mchorse.mappet.client.gui.utils;

import mchorse.mappet.Mappet;
import mchorse.mclib.client.gui.framework.elements.list.GuiStringListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.tooltips.LabelTooltip;
import mchorse.mclib.client.gui.framework.tooltips.styles.TooltipStyle;
import mchorse.mclib.client.gui.utils.Area;
import mchorse.mclib.client.gui.utils.Icon;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
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

    private final LabelTooltip parentTooltip = new LabelTooltip(IKey.EMPTY, Direction.TOP);

    public GuiStringFolderList(Minecraft mc, Consumer<List<String>> callback) {
        super(mc, null);
        this.callback = l -> handleClick(callback, l);
    }

    public void setFileIcon(Icon icon) {fileIcon = icon;}

    /* Hierarchy */

    public void fill(Set<String> files) {
        hierarchy.clear();
        hierarchy.addAll(files);

        while (!folderPath.isEmpty() && !folderExists(folderPath)) {
            String p = parent(folderPath);
            folderPath = p == null ? "" : p;
        }
        rebuild();
    }

    public boolean exists(String path) {return path != null && hierarchy.contains(path);}

    // safe method
    public boolean folderExists(String folderPath) {
        return folderPath != null && exists(folderPath.endsWith("/") ? folderPath : folderPath + "/");
    }

    public void addFile(String path) {
        if (path == null || !inFolder(path)) return;
        hierarchy.add(path);
        add(filename(path));
        sort();
        selectFile(path);
    }

    public void removeFile(String path) {
        if (!inFolder(path)) return;
        hierarchy.remove(path);
        remove(filename(path));
        unselectFile();
    }

    public String filename(String filePath) {
        if (filePath == null) return null;
        int i = filePath.lastIndexOf('/');
        return i < 0 ? filePath : filePath.substring(i + 1);
    }

    public boolean inFolder(String filePath) {
        if (!exists(filePath)) return false;
        if (folderPath.isEmpty()) return filePath.indexOf('/') < 0;
        return filePath.startsWith(folderPath + "/") && filePath.indexOf('/', folderPath.length() + 1) < 0;
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

    private static String parent(String path) {
        if (path.isEmpty()) return null;
        int i = path.lastIndexOf('/');
        return i < 0 ? "" : path.substring(0, i);
    }

    /* Navigation */

    public String getFolder() {return folderPath;}

    public String getPath(String name) {return folderPath.isEmpty() ? name : folderPath + "/" + name;}

    public void root() {goTo("");}

    public void up() {goTo(folderPath.isEmpty() ? "" : parent(folderPath));}

    public void down(String folderName) {goTo(getPath(folderName));}

    public void goTo(String path) {
        if (path == null) return;
        folderPath = path;
        if(!path.isEmpty()) parentTooltip.label = IKey.str(path + "/../");
        filter("");
        unselectFile();
        rebuild();
    }

    /* Selection */

    public String getFile() {
        String current = getCurrentFirst();
        return current == null || current.endsWith("/") ? null : getPath(current);
    }

    public void selectFile(String filePath) {
        if (filePath == null) return;
        int i = filePath.lastIndexOf('/');
        goTo(i < 0 ? "" : filePath.substring(0, i));
        setCurrentScroll(i < 0 ? filePath : filePath.substring(i + 1));
    }

    public void unselectFile() {current.clear();}

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

    /* Rendering */

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