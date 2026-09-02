package mchorse.mappet.client.gui.panels;

import mchorse.mappet.MappetIcons;
import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.api.utils.content.IContentType;
import mchorse.mappet.client.gui.GuiLabel;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.utils.GuiConfirmOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiPromptOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiStringFolderList;
import mchorse.mappet.client.gui.utils.GuiStringFolderSearchListElement;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.*;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiContextMenu;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDrawable;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanel;
import mchorse.mclib.client.gui.utils.GuiUtils;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.util.Set;

public abstract class GuiMappetDashboardPanel<T extends AbstractData> extends GuiDashboardPanel<GuiMappetDashboard> {
    public static final IKey KEYS_CATEGORY = IKey.lang("mappet.gui.panels.keys.category");

    public GuiElement iconBar;
    public GuiIconElement toggleSidebar;
    public GuiElement sidebar;

    public GuiElement buttons;
    public GuiIconElement add;
    public GuiIconElement dupe;
    public GuiIconElement rename;
    public GuiIconElement remove;
    public GuiStringFolderSearchListElement folderSearch;
    public GuiStringFolderList folderList;

    public GuiElement editor;
    protected boolean update;

    protected T data;

    protected String waitingId = "";
    protected GuiElement syncIndicator;

    protected boolean allowed;
    protected boolean save;

    public GuiMappetDashboardPanel(Minecraft mc, GuiMappetDashboard dashboard) {
        super(mc, dashboard);

        buttons = new GuiElement(mc);

        sidebar = new GuiElement(mc);
        sidebar.flex().relative(this).w(200).h(1F);
        iconBar = new GuiElement(mc);
        iconBar.flex().relative(sidebar).x(1F).w(20).h(1F).column(0).stretch();

        toggleSidebar = new GuiIconElement(mc, Icons.LEFTLOAD, (element) -> toggleSidebar());
        iconBar.add(toggleSidebar);

        add = new GuiIconElement(mc, Icons.ADD, this::addData);
        dupe = new GuiIconElement(mc, Icons.DUPE, this::dupeData);
        rename = new GuiIconElement(mc, Icons.EDIT, this::renameData);
        remove = new GuiIconElement(mc, Icons.REMOVE, this::removeData);

        GuiDrawable drawable = new GuiDrawable(
                (context) -> font.drawStringWithShadow(I18n.format(getTitle()), folderSearch.area.x, area.y + 10, 0xffffff));

        folderSearch = new GuiStringFolderSearchListElement(mc, (list) -> pickData(list.get(0)));
        folderList = (GuiStringFolderList) folderSearch.list;
        folderSearch.label(IKey.lang("mappet.gui.search"));
        folderSearch.flex().relative(sidebar).xy(10, 25).w(1F, -20).h(1F, -35);
        folderSearch.list.context(this::sidebarContext);
        sidebar.add(drawable, folderSearch, buttons);

        editor = new GuiElement(mc);
        editor.flex().relative(this).x(1F).w(1F, -220).anchorX(1).h(1F);

        buttons.flex().relative(folderSearch).x(1F).y(-20).anchorX(1F).row(0).resize();
        buttons.add(add, dupe, rename, remove);

        IKey sik = IKey.lang("sync");
        syncIndicator = new GuiLabel(mc, sik, MappetIcons.SYNC, null).background(0x66000000);
        syncIndicator.flex().relative(this).x(1F, -20).anchorX(1).y(0F, 20).w(MappetIcons.SYNC.w + 5 + font.getStringWidth(sik.get())).h(16);

        markContainer();
        add(sidebar, iconBar, editor, syncIndicator);


        keys().register(IKey.lang("mappet.gui.panels.keys.toggle_sidebar"), Keyboard.KEY_N,
                        () -> toggleSidebar.clickItself(GuiBase.getCurrent())).category(KEYS_CATEGORY);
    }

    @Override
    public boolean mouseClicked(GuiContext context) {
        if (sidebar.mouseClicked(context) || iconBar.mouseClicked(context)) return true;
        return super.mouseClicked(context);
    }

    protected GuiContextMenu sidebarContext() {
        GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc);

        if (data != null) menu.action(Icons.COPY, IKey.lang("mappet.gui.panels.context.copy"), this::copy);

        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(GuiScreen.getClipboardString());
            if (tag.getString("_ContentType").equals(getType().name()))
                menu.action(Icons.PASTE, IKey.lang("mappet.gui.panels.context.paste"), () -> paste(tag));
        } catch (Exception ignored) {}

        if (mc.isSingleplayer()) menu.action(Icons.FOLDER, IKey.lang("mappet.gui.panels.context.open_folder"), () -> {
            String path = getType().manager().getFolder().getAbsolutePath() + "/" + folderList.getFolder();
            GuiUtils.openFolder(path);
        });

        menu.action(Icons.ADD, IKey.lang("mappet.gui.panels.context.add_folder"), this::addFolder);
        if (!folderList.getFolder().isEmpty()) {
            menu.action(Icons.EDIT, IKey.lang("mappet.gui.panels.context.rename_folder"), this::renameFolder);
            menu.action(Icons.REMOVE, IKey.lang("mappet.gui.panels.context.remove_folder"), this::removeFolder);
        }

        return menu.actions.getList().isEmpty() ? null : menu.shadow();
    }

    private void copy() {
        NBTTagCompound tag = data.serializeNBT();
        tag.setString("_ContentType", getType().name());
        GuiScreen.setClipboardString(tag.toString());
    }

    private void paste(NBTTagCompound tag) {
        tag.removeTag("_ContentType");
        addData(add, getType().manager().create("", tag));
    }

    private void toggleSidebar() {
        sidebar.toggleVisible();
        boolean visible = sidebar.isVisible();
        toggleSidebar.both(visible ? Icons.LEFTLOAD : Icons.RIGHTLOAD);
        editor.flex().w(1F, visible ? -220 : -20);
        iconBar.flex().relative(sidebar).x(visible ? 1F : 0);
        resize();
    }

    public abstract IContentType<T> getType();

    public abstract String getTitle();

    public void pickData(String id) {
        save();
        Dispatcher.sendToServer(new PacketContentRequestData(getType(), id));
    }

    protected void waitAny() {
        wait("");
    }

    protected void wait(String path) {
        waitingId = path;
        syncIndicator.setVisible(path != null);
    }

    protected void addData(GuiIconElement element) {
        addData(element, null);
    }

    protected void addData(GuiIconElement element, T data) {
        new GuiPromptOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.add"), name -> addData(folderList.getPath(name), data),
                                  this::hasDuplicate).filename().open();
    }

    protected void addData(String id, T newData) {
        if (folderList.exists(id)) return;

        save();

        if (newData == null) {
            newData = getType().manager().create(id);
            fillDefaultData(newData);
        }
        else {
            newData.setId(id);
        }

        Dispatcher.sendToServer(new PacketContentData(getType(), id, newData.serializeNBT()));
        folderList.addFile(id);
        fill(newData);
    }

    private void addFolder() {
        new GuiPromptOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.add_folder"), this::addFolder, this::hasDuplicateFolder).filename()
                                                                                                                                  .open();
    }

    private void addFolder(String name) {
        Dispatcher.sendToServer(new PacketContentFolder(getType(), folderList.getPath(name)));
    }

    private void renameFolder() {
        if (folderList.getFolder().isEmpty()) return;
        new GuiPromptOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.rename_folder"), this::renameFolder,
                                  this::hasDuplicateFolder).filename().setValue(folderList.getFolder()).open();
    }

    private void renameFolder(String name) {
        Dispatcher.sendToServer(new PacketContentRename(getType(), folderList.getFolder(), folderList.getPath(name + '/')));
        fill(null);
    }

    private void removeFolder() {
        if (folderList.getFolder().isEmpty()) return;
        new GuiConfirmOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.remove_folder"),
                                   IKey.str(TextFormatting.RED + folderList.getFolder()), this::removeFolder).open();
    }

    private void removeFolder(Boolean isDelete) {
        if (isDelete) Dispatcher.sendToServer(new PacketContentFolder(getType(), folderList.getFolder()).delete());
    }

    protected void fillDefaultData(T data) {}

    protected void dupeData(GuiIconElement element) {
        if (data == null) return;
        new GuiPromptOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.dupe"), this::dupeData, this::hasDuplicate).filename()
                                                                                                                     .setValue(folderList.filename(data.getId()))
                                                                                                                     .open();
    }

    protected void dupeData(String name) {
        String id = folderList.getPath(name);
        if (folderList.exists(id)) return;
        save();
        T duped = getType().manager().create(id, data.serializeNBT());
        Dispatcher.sendToServer(new PacketContentData(getType(), id, duped.serializeNBT()));
        folderList.addFile(id);
        fill(duped);
    }

    protected void renameData(GuiIconElement element) {
        if (data == null) return;
        new GuiPromptOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.rename"), this::renameData, this::hasDuplicate).filename()
                                                                                                                         .setValue(folderList.filename(data.getId()))
                                                                                                                         .open();
    }

    protected void renameData(String name) {
        String id = folderList.getPath(name);
        if (data == null || folderList.exists(id)) return;
        Dispatcher.sendToServer(new PacketContentRename(getType(), data.getId(), id));
        folderList.removeFile(data.getId());
        folderList.addFile(id);
        data.setId(id);
    }

    // todo: minimize
    protected IKey hasDuplicate(String name) {
        if (name == null || name.isEmpty()) return IKey.lang("mappet.gui.panels.error.empty");
        return folderList.exists(folderList.getPath(name)) ? IKey.lang("mappet.gui.panels.error.duplicate") : null;
    }

    protected IKey hasDuplicateFolder(String name) {
        if (name == null || name.isEmpty()) return IKey.lang("mappet.gui.panels.error.empty");
        return folderList.folderExists(folderList.getPath(name)) ? IKey.lang("mappet.gui.panels.error.duplicate") : null;
    }

    protected void removeData(GuiIconElement element) {
        if (data == null) return;
        new GuiConfirmOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.remove"), IKey.str(TextFormatting.RED + data.getId()),
                                   this::removeData).open();
    }

    protected void removeData(boolean confirm) {
        if (!confirm || data == null) return;
        Dispatcher.sendToServer(new PacketContentData(getType(), data.getId(), null));
        folderList.removeFile(data.getId());
        fill(null);
    }

    /* Data population */

    public final void fill(T data) {
        fill(data, null);
    }

    public void fill(T data, String editorName) {
        this.data = data;
        this.allowed = editorName == null || editorName.equals(mc.player.getName());

        boolean hasData = data != null;
        editor.setEnabled(allowed);
        remove.setEnabled(allowed && hasData);
        rename.setEnabled(allowed && hasData);
        dupe.setEnabled(hasData);

        wait(null);
    }

    public void fillPaths(Set<String> names) {
        fillPaths(names, null, null);
    }

    public void fillPaths(Set<String> paths, String renameOld, String renameNew) {
        folderList.fill(paths);

        if(data == null) {
            if (renameOld != null && renameNew != null && renameOld.endsWith("/")) { // is folder rename
                String currentFolder = folderList.getFolder();
                if (!currentFolder.isEmpty() && currentFolder.startsWith(renameOld)) {
                    String relocated = renameNew + currentFolder.substring(renameOld.length());
                    folderList.goTo(relocated);
                }
            }
        }
        else {
            if (renameNew != null && data.getId().equals(renameOld)) data.setId(renameNew);
            folderList.selectFile(data.getId());
        }

        wait(null);
    }

    protected GuiScrollElement createScrollEditor() {
        GuiScrollElement scrollEditor = new GuiScrollElement(mc);
        scrollEditor.flex().relative(editor).wh(1F, 1F).column(5).stretch().vertical().scroll().padding(10);
        return scrollEditor;
    }

    @Override
    public void open() {
        update = true;
        save = true;
    }

    @Override
    public void appear() {
        waitAny();
        if (update) {
            update = false;
            Dispatcher.sendToServer(new PacketContentRequestPaths(getType()));
        }
        if (data != null) Dispatcher.sendToServer(new PacketContentRequestData(getType(), data.getId()));
    }

    @Override
    public void disappear() {
        if (save) save();
    }

    @Override
    public void close() {
        if (save) save();
    }

    public void save() {
        if (!save || update || data == null || !editor.isEnabled()) return;
        waitAny();
        preSave();
        Dispatcher.sendToServer(new PacketContentData(getType(), data.getId(), data.serializeNBT()));
    }

    protected void preSave() {}

    @Override
    public void draw(GuiContext context) {
        iconBar.area.draw(0xaa000000);
        if (sidebar.isVisible()) sidebar.area.draw(0xaa000000);
        super.draw(context);
        if (!editor.isEnabled() && data != null) GuiDraw.drawLockedArea(editor);
    }
}