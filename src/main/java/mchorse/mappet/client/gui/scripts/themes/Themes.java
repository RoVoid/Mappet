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
        if (editorThemes != null) return;

        editorThemes = new File(ClientProxy.configFolder, "themes");
        editorThemes.mkdirs();

        File monokai = new File(editorThemes, "monokai.json");
        File dracula = new File(editorThemes, "dracula.json");
        File vscode = new File(editorThemes, "vscode.json");
        File jetbrains = new File(editorThemes, "jetbrains.json");

        if (!monokai.isFile()) {
            writeTheme(monokai, new SyntaxStyle());
        }

        if (!dracula.isFile()) {
            SyntaxStyle draculaStyle = new SyntaxStyle("Dracula", true, 0xf92672, 0x66d9ef, 0xffc66d, 0xcc7832, 0x619554, 0x808080, 0x6694b8, 0xcc7832, 0x9876aa, 0xf92672, 0xa9b7c6, 0x5e6163, 0x2b2b2b);
            writeTheme(dracula, draculaStyle);
        }

        if (!vscode.isFile()) {
            SyntaxStyle vscodeStyle = new SyntaxStyle("VS Code", true, 0xff82d3, 0xCF84DF, 0x668cd4, 0x5fcadf, 0xddaf86, 0x5e9c6f, 0xCAD578, 0xffff9b, 0xb8dfec, 0xaaaaaa, 0xeaeaea, 0x6e7681, 0x1f1f1f);
            writeTheme(vscode, vscodeStyle);
        }

        if (!jetbrains.isFile()) {
            SyntaxStyle jetbrainsStyle = new SyntaxStyle("JetBrains", true, 0xfd971f, 0xc9a638, 0xff7c4c, 0xc052bd, 0x8dd364, 0x7f7f7f, 0x82e6ff, 0x79a0ff, 0xffffda, 0xaaaaaa, 0xeaeaea, 0x626262, 0x222227);
            writeTheme(jetbrains, jetbrainsStyle);
        }
    }
}