package mchorse.mappet.api.scripts.code.ui;

import mchorse.mappet.api.scripts.code.ui.MappetUIBuilder;
import mchorse.mappet.api.ui.UIContext;
import mchorse.mappet.api.ui.utils.DiscardMethod;
import mchorse.mappet.client.gui.utils.GuiDropdownElement;
import mchorse.mclib.client.gui.framework.elements.GuiElement;
import mchorse.mclib.client.gui.utils.keys.IKey;
import mchorse.mclib.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * String list UI component.
 *
 * <p>This component allows users to pick a string out of a list of strings
 * that you provided.</p>
 *
 * <p>The value that gets written to UI context's data (if ID is present) is
 * the selected string that picked in the list.</p>
 *
 * <p>This component can be created using {@link MappetUIBuilder#stringList(List)} method.</p>
 *
 * <pre>{@code
 *    function main(c)
 *    {
 *        var s = c.getSubject();
 *        var ui = mappet.createUI(c, "handler").background();
 *        var strings = ui.stringList(["Apple", "Orange", "Pineapple", "Avocado"]).id("strings").tooltip("Pick a fruit...");
 *        var label = ui.label("...").id("fruit").visible(false);
 *
 *        strings.background(0x88000000).rxy(0.5, 0.5).wh(100, 240).anchor(0.5);
 *        label.rx(0.5).ry(0.5, -160).anchor(0.5, 0.5);
 *        label.background(0x88000000).labelAnchor(0.5, 0.5);
 *        s.openUI(ui);
 *    }
 *
 *    function handler(c)
 *    {
 *        var uiContext = c.getSubject().getUIContext();
 *        var data = uiContext.getData();
 *
 *        if (uiContext.getLast() === "strings")
 *        {
 *            uiContext.get("fruit").label(data.getString("strings")).visible(true);
 *        }
 *    }
 * }</pre>
 */
public class UIDropdownComponent extends UILabelBaseComponent {
    public List<String> values = new ArrayList<>();
    public Integer selected;
    public Integer background;
    public Integer listHeight;

    /**
     * Replace values within this string list.
     *
     * <pre>{@code
     *    // Assuming that uiContext is a MappetUIContext
     *
     *    // Replace values in strings
     *    uiContext.get("strings").values("Tomato", "Cucumber", "Pepper", "Cabbage");
     * }</pre>
     */
    public UIDropdownComponent values(String... values) {
        change("Values");

        this.values.clear();
        this.values.addAll(Arrays.asList(values));

        return this;
    }

    /**
     * Replace values within this string list.
     *
     * <pre>{@code
     *    // Assuming that uiContext is a MappetUIContext
     *    var vegetables = ["Tomato", "Cucumber", "Pepper", "Cabbage"];
     *
     *    // Replace values in strings
     *    uiContext.get("strings").values(vegetables);
     * }</pre>
     */
    public UIDropdownComponent values(List<String> values) {
        change("Values");

        this.values.clear();
        this.values.addAll(values);

        return this;
    }

    /**
     * Replace values within this string list.
     *
     * <pre>{@code
     *    // Assuming that uiContext is a MappetUIContext
     *    var vegetables = ["Tomato", "Cucumber", "Pepper", "Cabbage"];
     *
     *    // Replace values in strings
     *    uiContext.get("strings").setValues(vegetables);
     * }</pre>
     */
    public UIDropdownComponent setValues(List<String> values) {
        return values(values);
    }

    /**
     * Returns values of this string list.
     *
     * <pre>{@code
     *    var values = uiContext.get("strings").getValues();
     *
     *    for (var i in values)
     *    {
     *        c.send(values[i]);
     *    }
     * }</pre>
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Set the currently selected element.
     *
     * <pre>{@code
     *    // Assuming that uiContext is a MappetUIContext
     *
     *    // Set first string in the list to be selected
     *    uiContext.get("strings").selected(0);
     * }</pre>
     */
    public UIDropdownComponent selected(int selected) {
        change("Selected");

        this.selected = selected;

        return this;
    }

    /**
     * Set component's solid color background.
     *
     * <pre>{@code
     *    // Assuming that uiContext is a MappetUIContext
     *
     *    // Set half transparent black background
     *    uiContext.get("strings").background();
     * }</pre>
     */
    public UIDropdownComponent background() {
        return background(ColorUtils.HALF_BLACK);
    }

    /**
     * Set component's solid color background.
     *
     * <pre>{@code
     *    // Assuming that uiContext is a MappetUIContext
     *
     *    // Set half transparent toxic green background
     *    uiContext.get("strings").background(0x8800ff00);
     * }</pre>
     */
    public UIDropdownComponent background(int background) {
        change("Background");

        this.background = background;

        return this;
    }

    public UIDropdownComponent listHeight(int listHeight) {
        change("ListHeight");
        this.listHeight = listHeight;
        return this;
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    protected void applyProperty(UIContext context, String key, GuiElement element) {
        super.applyProperty(context, key, element);

        GuiDropdownElement dropdown = (GuiDropdownElement) element;

        if (key.equals("Values")) {
            dropdown.list.clear();
            dropdown.list.add(values);
        }
        else if (key.equals("Selected")) {
            dropdown.label = IKey.str(isSelected() ? values.get(selected) : label);
            dropdown.list.setIndex(isSelected() ? selected : -1);
        }
        else if (key.equals("Background") && background != null) {
            dropdown.list.background(background);
        }
        else if (key.equals("ListHeight") && listHeight != null) {
            dropdown.height = listHeight;
            dropdown.list.resize();
        }
    }

    private boolean isSelected() {
        return selected != null && selected >= 0 && selected < values.size();
    }

    @Override
    @DiscardMethod
    @SideOnly(Side.CLIENT)
    public GuiElement create(Minecraft mc, UIContext context) {
        GuiDropdownElement element = new GuiDropdownElement(mc, isSelected() ? values.get(selected) : label, null);
        element.listCallback = (s) -> {
            if (id.isEmpty()) return;
            context.data.setString(id, s);
            context.data.setInteger(id + ".index", element.list.getIndex());
            context.dirty(id, updateDelay);
        };

        element.list.add(values);

        if (selected != null) element.list.setIndex(selected);

        if (background != null) element.list.background(background);

        if (listHeight != null) {
            element.height = listHeight;
            element.list.resize();
        }

        return apply(element, context);
    }

    @Override
    @DiscardMethod
    public void populateData(NBTTagCompound tag) {
        super.populateData(tag);

        if (id.isEmpty()) return;

        String value = "";
        int index = -1;

        if (isSelected()) {
            value = values.get(selected);
            index = selected;
        }

        tag.setInteger(id + ".index", index);
        tag.setString(id, value);
    }

    @Override
    @DiscardMethod
    public void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);

        NBTTagList list = new NBTTagList();
        for (String value : values) list.appendTag(new NBTTagString(value));
        if (list.tagCount() > 0 || changedProperties.contains("Values")) tag.setTag("Values", list);

        if (selected != null) tag.setInteger("Selected", selected);
        if (background != null) tag.setInteger("Background", background);
        if (listHeight != null) tag.setInteger("LineHeight", listHeight);
    }

    @Override
    @DiscardMethod
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);

        values.clear();
        if (tag.hasKey("Values")) {
            NBTTagList list = tag.getTagList("Values", Constants.NBT.TAG_STRING);
            for (int i = 0, c = list.tagCount(); i < c; i++) values.add(list.getStringTagAt(i));
        }

        if (tag.hasKey("Selected")) selected = tag.getInteger("Selected");
        if (tag.hasKey("Background")) background = tag.getInteger("Background");
        if (tag.hasKey("LineHeight")) listHeight = tag.getInteger("LineHeight");
    }
}