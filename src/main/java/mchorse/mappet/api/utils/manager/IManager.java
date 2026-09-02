package mchorse.mappet.api.utils.manager;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.io.File;
import java.util.Set;

public interface IManager<T extends INBTSerializable<NBTTagCompound>> {
    default T create(String id) {
        return create(id, null);
    }

    T create(String id, NBTTagCompound tag);

    // don't forget '.' at beginning of extension
    String getExtension();

    T load(String id);

    boolean exists(String id);

    boolean rename(String id, String newId);

    boolean save(String id, NBTTagCompound tag);

    boolean delete(String id);

    Set<String> getIDs();

    void addFolder(String path);

    void renameFolder(String oldPath, String newPath);

    void deleteFolder(String path);

    File getFolder();

    Set<String> getPaths();
}