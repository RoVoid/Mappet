package mchorse.mappet.api.schematics;

import mchorse.mappet.Mappet;
import mchorse.mappet.MappetConfig;
import mchorse.mappet.api.utils.manager.BaseManager;
import mchorse.mappet.api.utils.manager.ManagerCache;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.io.IOException;

public class SchematicManager extends BaseManager<Schematic> {
    public SchematicManager(File folder) {
        super(folder);
    }

    @Override
    protected Schematic createData(String id, NBTTagCompound tag) {
        Schematic schematic = new Schematic(0, 0, 0);
        schematic.deserializeNBT(tag);
        return schematic;
    }

    @Override
    public boolean save(String id, Schematic data) {
        try {
            CompressedStreamTools.write(data.serializeNBT(), getFile(id));
        } catch (IOException e) {
            Mappet.logger.error(e.getMessage());

            return false;
        }

        return true;
    }

    @Override
    protected NBTTagCompound getCached(String id) throws Exception {
        NBTTagCompound tag = null;
        File file = getFile(id);
        boolean isCaching = MappetConfig.generalDataCaching.get();
        long lastUpdated = file.lastModified();

        if (isCaching) {
            ManagerCache cache = this.cache.get(id);

            if (cache != null) {
                /* This is necessary for update if the files were edited externally,
                 * because dashboard save will clear the cache for sure */
                if (cache.lastUpdated < lastUpdated) {
                    this.cache.remove(id);
                }
                else {
                    tag = cache.tag;

                    cache.update();
                }

                doExpirationCheck();
            }
        }

        if (tag == null) {
            tag = CompressedStreamTools.read(file);

            if (isCaching) {
                cache.put(id, new ManagerCache(tag, lastUpdated));
            }
        }

        return tag;
    }

    @Override
    protected String getExtension() {
        return ".schematic";
    }
}