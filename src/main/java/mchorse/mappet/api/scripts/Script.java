package mchorse.mappet.api.scripts;

import mchorse.mappet.api.utils.AbstractData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

public class Script extends AbstractData {
    public String code = "";
    public boolean unique = true;
    public boolean globalLibrary = false;
    public Set<String> libraries = new LinkedHashSet<>();

    public String getExtension() {
        String id = getId();
        int index = id.lastIndexOf('.');
        return index >= 0 ? id.substring(index + 1) : "js";
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList librariesNBT = new NBTTagList();

        for (String library : libraries) librariesNBT.appendTag(new NBTTagString(library));

        tag.setBoolean("Unique", unique);
        tag.setBoolean("GlobalLibrary", globalLibrary);
        tag.setTag("Libraries", librariesNBT);
        tag.setByteArray("Code", code.getBytes(StandardCharsets.UTF_8));

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        unique = tag.getBoolean("Unique");
        globalLibrary = tag.getBoolean("GlobalLibrary");

        if (tag.hasKey("Libraries", Constants.NBT.TAG_LIST)) {
            NBTTagList librariesNBT = tag.getTagList("Libraries", Constants.NBT.TAG_STRING);
            libraries.clear();
            for (int i = 0, c = librariesNBT.tagCount(); i < c; i++) libraries.add(librariesNBT.getStringTagAt(i));
        }

        code = new String(tag.getByteArray("Code"), StandardCharsets.UTF_8);
    }
}