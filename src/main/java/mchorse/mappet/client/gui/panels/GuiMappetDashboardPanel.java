package mchorse.mappet.client.gui.panels;

import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.utils.GuiConfirmOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiPromptOverlayPanel;
import mchorse.mappet.client.gui.utils.GuiStringFolderList;
import mchorse.mappet.client.gui.utils.GuiStringFolderSearchListElement;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.content.PacketContentData;
import mchorse.mappet.network.packets.content.PacketContentFolder;
import mchorse.mappet.network.packets.content.PacketContentRequestData;
import mchorse.mappet.network.packets.content.PacketContentRequestNames;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiContextMenu;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.modals.GuiModal;
import mchorse.mclib.client.gui.framework.elements.modals.GuiPromptModal;
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
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.input.Keyboard;

import java.util.List;

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

        add = new GuiIconElement(mc, Icons.ADD, this::addNewData);
        add.context(() -> {
            GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc);

            menu.action(Icons.ADD, IKey.lang("mappet.gui.panels.context.add_folder"), this::addFolder);

            return menu.shadow();
        });
        dupe = new GuiIconElement(mc, Icons.DUPE, this::dupeData);
        rename = new GuiIconElement(mc, Icons.EDIT, this::renameData);
        rename.context(() -> {
            if (folderList.getPath().isEmpty()) return null;

            GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc);

            menu.action(Icons.EDIT, IKey.lang("mappet.gui.panels.context.rename_folder"), this::renameFolder);

            return menu.shadow();
        });
        remove = new GuiIconElement(mc, Icons.REMOVE, this::removeData);
        remove.context(() -> {
            if (folderList.getPath().isEmpty()) return null;
            GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc);
            menu.action(Icons.REMOVE, IKey.lang("mappet.gui.panels.context.remove_folder"), this::removeFolder);
            return menu.shadow();
        });

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

        markContainer();
        add(sidebar, iconBar, editor);

        keys().register(IKey.lang("mappet.gui.panels.keys.toggle_sidebar"), Keyboard.KEY_N,
                () -> toggleSidebar.clickItself(GuiBase.getCurrent())).category(KEYS_CATEGORY);
    }

    @Override
    public boolean mouseClicked(GuiContext context) {
        if (sidebar.mouseClicked(context)) return true;
        return super.mouseClicked(context);
    }

    protected GuiContextMenu sidebarContext() {
        GuiSimpleContextMenu menu = new GuiSimpleContextMenu(mc);

        if (data != null) menu.action(Icons.COPY, IKey.lang("mappet.gui.panels.context.copy"), this::copy);

        try {
            NBTTagCompound tag = JsonToNBT.getTagFromJson(GuiScreen.getClipboardString());

            if (tag.getString("_ContentType").equals(getType().name()))
                menu.action(Icons.PASTE, IKey.lang("mappet.gui.panels.context.paste"), () -> paste(tag));
        } catch (Exception ignored) {
        }

        if (mc.isSingleplayer()) menu.action(Icons.FOLDER, IKey.lang("mappet.gui.panels.context.open_folder"), () -> {
            String path = getType().manager().getFolder().getAbsolutePath() + "/" + folderList.getPath();

            GuiUtils.openFolder(path);
        });

        return menu.actions.getList().isEmpty() ? null : menu.shadow();
    }

    private void copy() {
        NBTTagCompound tag = data.serializeNBT();

        tag.setString("_ContentType", getType().name());
        GuiScreen.setClipboardString(tag.toString());
    }

    private void paste(NBTTagCompound tag) {
        T data = (T) getType().manager().create("", tag);
        addNewData(add, data);
    }

    private void toggleSidebar() {
        sidebar.toggleVisible();
        boolean visible = sidebar.isVisible();
        toggleSidebar.both(visible ? Icons.LEFTLOAD : Icons.RIGHTLOAD);

        editor.flex().w(1F, visible ? -220 : -20);
        iconBar.flex().relative(sidebar).x(visible ? 1F : 0);

        resize();
    }

    /**
     * Get the content type of this panel
     */
    public abstract IContentTypeBase getType();

    public abstract String getTitle();

    public void pickData(String id) {
        save();
        Dispatcher.sendToServer(new PacketContentRequestData(getType(), id));
    }

    /* CRUD */

    protected void addNewData(GuiIconElement element) {
        addNewData(element, null);
    }

    protected void addNewData(GuiIconElement element, T data) {
        GuiModal.addFullModal(sidebar, () -> new GuiPromptModal(mc, IKey.lang("mappet.gui.panels.modals.add"),
                (name) -> addNewData(folderList.getPath(name), data)).filename());
    }

    protected void addNewData(String name, T data) {
        if (folderList.notInHierarchy(name)) {
            save();

            Dispatcher.sendToServer(new PacketContentData(getType(), name, data == null ? new NBTTagCompound() : data.serializeNBT()));

            folderList.addFile(name);

            if (data == null) {
                data = (T) getType().manager().create(name);
                fillDefaultData(data);
                getType().manager().create(data.getId(), data.serializeNBT());
            }
            else data.setId(name);

            fill(data);
        }
    }

    private void addFolder() {
        GuiModal.addFullModal(sidebar,
                () -> new GuiPromptModal(mc, IKey.lang("mappet.gui.panels.modals.add_folder"), this::addFolder).filename());
    }

    private void addFolder(String name) {
        Dispatcher.sendToServer(new PacketContentFolder(getType(), name, folderList.getPath("")));
    }

    private void renameFolder() {
        if (folderList.getPath().isEmpty()) return;

        String name = FilenameUtils.getBaseName(folderList.getPath());
        GuiModal.addModal(sidebar,
                () -> new GuiPromptModal(mc, IKey.lang("mappet.gui.panels.modals.rename_folder"), this::renameFolder).filename()
                        .setValue(name));
        //        GuiModal.addFullModal(
        //                sidebar, () -> new GuiPromptModal(mc, IKey.lang("mappet.gui.panels.modals.rename_folder"), this::renameFolder).filename()
        //                        .setValue(name));
    }

    private void renameFolder(String name) {
        String path = folderList.getPath("");

        Dispatcher.sendToServer(new PacketContentFolder(getType(), "", path.substring(0, path.length() - 1)).rename(name));
        fill(null);
    }

    private void removeFolder() {
        if (!folderList.getPath().isEmpty()) return;

        new GuiConfirmOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.remove_folder"), this::removeFolder).open();
    }

    private void removeFolder(Boolean isDelete) {
        if (isDelete) {
            String path = folderList.getPath("");
            Dispatcher.sendToServer(new PacketContentFolder(getType(), "", path.substring(0, path.length() - 1)).delete());
        }
    }

    protected void fillDefaultData(T data) {
    }

    protected void dupeData(GuiIconElement element) {
        if (data == null) return;

        GuiModal.addFullModal(sidebar, () -> {
            GuiPromptModal promptModal = new GuiPromptModal(mc, IKey.lang("mappet.gui.panels.modals.dupe"), this::dupeData);

            return promptModal.setValue(data.getId()).filename();
        });
    }

    protected void dupeData(String name) {
        if (folderList.notInHierarchy(name)) {
            save();

            Dispatcher.sendToServer(new PacketContentData(getType(), name, data.serializeNBT()));

            folderList.addFile(name);

            T data = (T) getType().manager().create(name, this.data.serializeNBT());

            fill(data);
        }
    }

    protected void renameData(GuiIconElement element) {
        if (data != null)
            new GuiPromptOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.rename"), this::renameData, this::hasDuplicate).filename()
                    .setValue(folderList.filename(data.getId()))
                    .open();
    }

    protected void renameData(String name) {
        if (!folderList.notInHierarchy(name)) return;
        String path = getDataPath();
        Dispatcher.sendToServer(new PacketContentData(getType(), data.getId(), data.serializeNBT()).rename(path + name));

        folderList.removeFile(data.getId());
        folderList.addFile(path + name);

        data.setId(path + name);
    }

    private IKey hasDuplicate(String name) {
        if (name == null || name.isEmpty()) return IKey.lang("mappet.gui.panels.error.empty");
        return folderList.notInHierarchy(folderList.getPath(name)) ? null : IKey.lang("mappet.gui.panels.error.duplicate");
    }

    protected String getDataPath() {
        String output = "";
        int index = data.getId().lastIndexOf('/');

        if (index != -1) output = data.getId().substring(0, index + 1);

        return output;
    }

    protected void removeData(GuiIconElement element) {
        if (data != null) new GuiConfirmOverlayPanel(mc, IKey.lang("mappet.gui.panels.modals.remove"), this::removeData).open();
    }

    protected void removeData(boolean confirm) {
        if (data == null || !confirm) return;
        Dispatcher.sendToServer(new PacketContentData(getType(), data.getId(), null));
        folderList.removeFile(data.getId());
        fill(null);
    }

    /* Data population */

    public final void fill(T data) {
        fill(data, true);
    }

    public void fill(T data, boolean allowed) {
        this.data = data;
        this.allowed = allowed;
        editor.setEnabled(allowed);
        remove.setEnabled(allowed);
        rename.setEnabled(allowed);
    }

    public void fillNames(List<String> names) {
        String value = data == null ? null : data.getId();

        folderList.fill(names);
        folderList.sort();
        folderList.setCurrentFile(value);
    }

    protected GuiScrollElement createScrollEditor() {
        GuiScrollElement scrollEditor = new GuiScrollElement(mc);

        scrollEditor.flex().relative(editor).wh(1F, 1F).column(5).stretch().vertical().scroll().padding(10);

        return scrollEditor;
    }

    @Override
    public void open() {
        super.open();

        update = true;
        save = true;
    }

    @Override
    public void appear() {
        if (update) {
            update = false;
            requestDataNames();
        }
        if (data != null) Dispatcher.sendToServer(new PacketContentRequestData(getType(), data.getId()));
    }

    public void requestDataNames() {
        Dispatcher.sendToServer(new PacketContentRequestNames(getType()));
    }

    @Override
    public void disappear() {
        super.disappear();
        if (save) save();
    }

    @Override
    public void close() {
        super.close();
        if (save) save();
    }

    public void save() {
        if (update || data == null || !editor.isEnabled()) return;
        preSave();
        Dispatcher.sendToServer(new PacketContentData(getType(), data.getId(), data.serializeNBT()));
    }

    protected void preSave() {}

    @Override
    public void draw(GuiContext context) {
        iconBar.area.draw(0xaa000000);
//        GuiDraw.drawHorizontalGradientRect(iconBar.area.x - 6, iconBar.area.y, iconBar.area.x, iconBar.area.ey(), 0, 0x29000000);

        if (sidebar.isVisible()) sidebar.area.draw(0xaa000000);

        super.draw(context);

        if (!editor.isEnabled() && data != null) GuiDraw.drawLockedArea(editor);
    }
}