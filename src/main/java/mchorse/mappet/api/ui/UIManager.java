package mchorse.mappet.api.ui;

import mchorse.mappet.api.utils.manager.BaseManager;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;

public class UIManager extends BaseManager<UI> {
    public UIManager(File folder) {
        super(folder);
    }

    @Override
    protected UI createData(String id, NBTTagCompound tag) {
        return null;
    }
}
