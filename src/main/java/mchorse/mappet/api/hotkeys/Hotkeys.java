package mchorse.mappet.api.hotkeys;

import mchorse.mappet.api.utils.DataContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Hotkeys implements INBTSerializable<NBTTagList> {
    public Map<String, Hotkey> keys = new HashMap<>();

    public void execute(EntityPlayer player, Set<HotkeyState> hotkeyStates) {
        for (HotkeyState hotkeyState : hotkeyStates) {
            Hotkey hotkey = keys.get(hotkeyState.name);
            if (hotkey != null) hotkey.execute(new DataContext(player)
                    .set("keyName", hotkey.name)
                    .set("keyMode", hotkey.mode.ordinal())
                    .set("keyState", hotkeyState.state ? 1 : 0));
        }
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList tag = new NBTTagList();
        for (Hotkey hotkey : keys.values()) {
            tag.appendTag(hotkey.serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagList tag) {
        keys.clear();
        if (tag == null) return;
        for (NBTBase nbt : tag) {
            if (nbt.getId() != 10) continue;
            Hotkey hotkey = new Hotkey();
            hotkey.deserializeNBT((NBTTagCompound) nbt);
            keys.put(hotkey.name, hotkey);
        }
    }
}