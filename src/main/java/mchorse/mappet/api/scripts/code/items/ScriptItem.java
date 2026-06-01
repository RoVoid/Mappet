package mchorse.mappet.api.scripts.code.items;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ScriptItem {
    private final Item item;

    public ScriptItem(Item item) {
        this.item = item;
    }

    @Deprecated
    public Item getMinecraftItem() {
        return item;
    }

    public Item asMinecraft() {
        return item;
    }

    public String getId() {
        ResourceLocation location = item == null ? null : item.getRegistryName();
        return location == null ? "" : location.toString();
    }

    public boolean isSame(ScriptItem other) {
        return item == other.asMinecraft();
    }
}