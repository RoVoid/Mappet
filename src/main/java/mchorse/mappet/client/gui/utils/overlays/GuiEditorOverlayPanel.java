package mchorse.mappet.client.gui.utils.overlays;

import mchorse.mappet.utils.Colors;
import mchorse.mclib.client.gui.framework.elements.GuiScrollElement;
import mchorse.mclib.client.gui.framework.elements.context.GuiSimpleContextMenu;
import mchorse.mclib.client.gui.framework.elements.list.GuiListElement;
import mchorse.mclib.client.gui.utils.Icons;
import mchorse.mclib.client.gui.utils.keys.IKey;
import net.minecraft.client.Minecraft;

/**
 * General purpose overlay list editor of generic data
 */
public abstract class GuiEditorOverlayPanel <T> extends GuiOverlayPanel
{
    public GuiListElement<T> list;
    public GuiScrollElement editor;

    protected T item;

    public GuiEditorOverlayPanel(Minecraft mc, IKey title)
    {
        super(mc, title);

        list = createList(mc);
        list.context(() ->
        {
            GuiSimpleContextMenu menu = new GuiSimpleContextMenu(this.mc).action(Icons.ADD, getAddLabel(), this::addItem);

            if (!list.getList().isEmpty())
            {
                menu.action(Icons.REMOVE, getRemoveLabel(), this::removeItem, Colors.NEGATIVE);
            }

            return menu.shadow();
        });

        editor = new GuiScrollElement(mc);

        list.flex().relative(content).w(120).h(1F);
        editor.flex().relative(content).x(120).w(1F, -120).h(1F).column(5).vertical().stretch().scroll().padding(10);

        content.add(editor, list);
    }

    protected abstract GuiListElement<T> createList(Minecraft mc);

    protected IKey getAddLabel()
    {
        return IKey.EMPTY;
    }

    protected IKey getRemoveLabel()
    {
        return IKey.EMPTY;
    }

    protected void addItem()
    {
        addNewItem();
        list.update();
    }

    protected void addNewItem()
    {}

    protected void removeItem()
    {
        int index = list.getIndex();

        list.getList().remove(index);

        index = Math.max(index - 1, 0);
        T item = list.getList().isEmpty() ? null : list.getList().get(index);

        pickItem(item, true);
        list.update();
    }

    protected void pickItem(T item, boolean select)
    {
        this.item = item;

        editor.setVisible(item != null);

        if (item != null)
        {
            fillData(item);

            if (select)
            {
                list.setCurrentScroll(item);
            }

            resize();
        }
    }

    protected abstract void fillData(T item);
}