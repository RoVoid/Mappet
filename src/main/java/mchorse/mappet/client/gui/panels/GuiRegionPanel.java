package mchorse.mappet.client.gui.panels;

import mchorse.mappet.client.gui.GuiMappetDashboard;
import mchorse.mappet.client.gui.regions.GuiRegionEditor;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.blocks.PacketEditRegion;
import mchorse.mappet.blocks.tile.TileRegion;
import mchorse.mappet.utils.ReflectionUtils;
import mchorse.mclib.client.gui.framework.GuiBase;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.buttons.GuiIconElement;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.framework.elements.utils.GuiContext;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDraw;
import mchorse.mclib.client.gui.framework.elements.utils.GuiDrawable;
import mchorse.mclib.client.gui.mclib.GuiDashboardPanel;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class GuiRegionPanel extends GuiDashboardPanel<GuiMappetDashboard>
{
    public static final IKey EMPTY = IKey.lang("mappet.gui.region.info.empty");

    public GuiIconElement toggleSidebar;
    public GuiElement sidebar;
    public GuiTileRegionListElement tiles;

    public GuiScrollElement editor;
    public GuiRegionEditor region;

    protected TileRegion tile;
    protected boolean wasOpened;

    public GuiRegionPanel(Minecraft mc, GuiMappetDashboard dashboard)
    {
        super(mc, dashboard);

        sidebar = new GuiElement(mc);
        sidebar.flex().relative(this).x(1F).w(200).h(1F).anchorX(1F);

        toggleSidebar = new GuiIconElement(mc, Icons.RIGHTLOAD, (element) -> toggleSidebar());
        toggleSidebar.flex().relative(sidebar).x(-20);

        GuiDrawable drawable = new GuiDrawable((context) -> font.drawStringWithShadow(I18n.format(getTitle()), tiles.area.x, area.y + 10, 0xffffff));

        tiles = new GuiTileRegionListElement(mc, (list) -> fill(list.get(0), false));
        tiles.flex().relative(sidebar).xy(10, 25).w(1F, -20).h(1F, -35);
        sidebar.add(drawable, tiles);

        editor = new GuiScrollElement(mc);
        editor.markContainer();
        editor.flex().relative(this).w(240).h(1F).column(5).vertical().stretch().scroll().padding(10);

        region = new GuiRegionEditor(mc);

        editor.scroll.opposite = true;
        editor.add(region);

        add(sidebar, editor, toggleSidebar);

        keys().register(IKey.lang("mappet.gui.panels.keys.toggle_sidebar"), Keyboard.KEY_N, () -> toggleSidebar.clickItself(GuiBase.getCurrent())).category(GuiMappetDashboardPanel.KEYS_CATEGORY);

        fill(null, true);
    }

    private void toggleSidebar()
    {
        sidebar.toggleVisible();
        toggleSidebar.both(sidebar.isVisible() ? Icons.RIGHTLOAD : Icons.LEFTLOAD);

        if (sidebar.isVisible()) toggleSidebar.flex().relative(sidebar).x(-20);
        else toggleSidebar.flex().relative(this).x(1F, -20);

        resize();
    }

    public TileRegion getTile()
    {
        return tile;
    }

    public String getTitle()
    {
        return "mappet.gui.panels.regions";
    }

    /* Data population */

    public void fill(TileRegion tile, boolean ignoreSave)
    {
        if (!ignoreSave) save();

        if (tile != null && tile.isInvalid()) tile = null;

        this.tile = tile;

        editor.setVisible(tile != null);
        tiles.setCurrentScroll(tile);

        if (tile != null) region.set(tile.region);
    }

    public void fillTiles(Collection<TileEntity> tiles)
    {
        this.tiles.clear();

        if (tiles == null) return;

        for (TileEntity tile : tiles)
            if (tile instanceof TileRegion) this.tiles.add((TileRegion) tile);

        this.tiles.setCurrentScroll(tile);
    }

    @Override
    public boolean needsBackground()
    {
        return false;
    }

    @Override
    public void open()
    {
        super.open();
        fillTiles(ReflectionUtils.getGlobalTiles(mc.renderGlobal));
    }

    @Override
    public void appear()
    {
        super.appear();
        if (tile != null && tile.isInvalid()) fill(null, true);
        wasOpened = true;
    }

    @Override
    public void close()
    {
        super.close();
        save();
        wasOpened = false;
    }

    private void save()
    {
        if (tile != null && !tile.isInvalid() && wasOpened)
            Dispatcher.sendToServer(new PacketEditRegion(tile.getPos(), tile.region.serializeNBT()));
    }

    @Override
    public void draw(GuiContext context)
    {
        if (editor.isVisible())
        {
            Gui.drawRect(editor.area.x, editor.area.y, editor.area.mx(), editor.area.ey(), 0xbb000000);
            GuiDraw.drawHorizontalGradientRect(editor.area.mx(), editor.area.y, editor.area.x(1.25F), editor.area.ey(), 0xbb000000, 0);
        }

        if (sidebar.isVisible()) sidebar.area.draw(0xdd000000);

        super.draw(context);

        if (!editor.isVisible())
        {
            int w = (sidebar.isVisible() ? sidebar.area.x - area.x : area.w) / 2;
            int x = area.x + w / 2;

            GuiDraw.drawMultiText(font, EMPTY.get(), x, area.my(), 0xffffff, w, 12, 0.5F, 0.5F);
        }
    }

    public static class GuiTileRegionListElement extends GuiListElement<TileRegion>
    {
        public GuiTileRegionListElement(Minecraft mc, Consumer<List<TileRegion>> callback)
        {
            super(mc, callback);
        }

        @Override
        protected String elementToString(TileRegion element)
        {
            BlockPos pos = element.getPos();
            String first = element.region.shapes.isEmpty() ? "" : I18n.format("mappet.gui.shapes." + element.region.shapes.get(0).getType()) + " ";

            return first + "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
        }
    }
}