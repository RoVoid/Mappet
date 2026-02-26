package mchorse.mappet.api.translations;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mchorse.mappet.api.utils.AbstractData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

// key
public class Translation extends AbstractData {
    /**
     * locale : value
     **/
    public Map<String, String> entries = new Object2ObjectOpenHashMap<>();

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        NBTTagCompound entriesNBT = new NBTTagCompound();
        for (Map.Entry<String, String> entry : entries.entrySet()) entriesNBT.setString(entry.getKey(), entry.getValue());
        tag.setTag("Entries", entriesNBT);

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (tag == null) return;
        if (tag.hasKey("Entries", Constants.NBT.TAG_COMPOUND)) {
            entries.clear();
            NBTTagCompound entriesNBT = tag.getCompoundTag("Entries");
            for (String locale : entriesNBT.getKeySet()) entries.put(locale, entriesNBT.getString(locale));
        }
    }
}
