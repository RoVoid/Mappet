package mchorse.mappet.api.scripts.code.items;

import mchorse.mappet.api.scripts.user.items.IScriptItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ScriptItem implements IScriptItem {
    private final Item item;

    public ScriptItem(Item item) {
        this.item = item;
    }

    @Override
    @Deprecated
    public Item getMinecraftItem() {
        return item;
    }

    @Override
    public Item asMinecraft() {
        return item;
    }

    @Override
    public String getId() {
        ResourceLocation location = item == null ? null : item.getRegistryName();
        return location == null ? "" : location.toString();
    }

    @Override
    public boolean isSame(IScriptItem other) {
        return item == other.asMinecraft();
    }
}