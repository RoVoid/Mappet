package mchorse.mappet.client.gui.scripts.themes;

import mchorse.mappet.ClientProxy;
import mchorse.mappet.client.gui.scripts.style.SyntaxStyle;
import mchorse.mappet.utils.NBTToJsonLike;
import mchorse.mclib.client.gui.utils.GuiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Themes {
    private static File editorThemes;
    private static SyntaxStyle style;

    /**
     * Open editor themes folder
     */
    public static void open() {
        GuiUtils.openWebLink(editorThemes.toURI());
    }

    /**
     * Get all theme files
     */
    public static List<File> themes() {
        List<File> themes = new ArrayList<>();
        File[] files = editorThemes.listFiles();

        if (files == null) return themes;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                themes.add(file);
            }
        }
        return themes;
    }

    public static File getThemeFile(String name) {
        if (!name.endsWith(".json")) name += ".json";
        return new File(editorThemes, name);
    }

    /**
     * Read theme out of the file
     */
    public static SyntaxStyle readTheme(File file) {
        try {
            return new SyntaxStyle(NBTToJsonLike.read(file));
        } catch (Exception ignored) {
        }

        return null;
    }

    /**
     * Write a theme into a file
     */
    public static void writeTheme(File file, SyntaxStyle style) {
        try {
            NBTToJsonLike.write(file, style.toNBT());
        } catch (Exception ignored) {
        }
    }

    /**
     * Initiate themes system
     */
    public static void initiate() {
        /* Just in case */
        if (editorThemes != null) return;

        editorThemes = new File(ClientProxy.configFolder, "themes");
        editorThemes.mkdirs();

        File monokai = new File(editorThemes, "monokai.json");
        File dracula = new File(editorThemes, "dracula.json");
        File vscode = new File(editorThemes, "vscode.json");

        if (!monokai.isFile()) {
            writeTheme(monokai, new SyntaxStyle());
        }

        if (!dracula.isFile()) {
            SyntaxStyle draculaStyle = new SyntaxStyle("Dracula", true, 0xf92672, 0x66d9ef, 0xffc66d, 0xcc7832, 0x619554, 0x808080, 0x6694b8, 0xcc7832, 0x9876aa, 0xf92672, 0xa9b7c6, 0x5e6163, 0x2b2b2b);
            writeTheme(dracula, draculaStyle);
        }

        if (!vscode.isFile()) {
            SyntaxStyle vscodeStyle = new SyntaxStyle("VS Code", true, 0xe280ff, 0x66d9ef, 0xdfe78c, 0xc2a573, 0xe6a67f, 0x5e9955, 0xb380ff, 0x61d9fa, 0xa6e22e, 0xf92672, 0xededfe, 0x556368, 0x212121);
            writeTheme(vscode, vscodeStyle);
        }
    }
}