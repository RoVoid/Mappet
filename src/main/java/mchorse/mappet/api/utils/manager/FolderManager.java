package mchorse.mappet.api.utils.manager;

import mchorse.mappet.MappetConfig;
import mchorse.mappet.api.utils.AbstractData;

import java.io.File;
import java.util.*;

/**
 * Folder based manager
 */
public abstract class FolderManager<T extends AbstractData> implements IManager<T> {
    protected Map<String, ManagerCache> cache = new HashMap<>();
    protected File folder;
    protected long lastCheck;

    public FolderManager(File folder) {
        if (folder != null) {
            this.folder = folder;
            this.folder.mkdirs();
        }
    }

    protected void doExpirationCheck() {
        final int threshold = 1000 * 30;
        long current = System.currentTimeMillis();

        /* Check every 30 seconds all cached entries and remove those that weren't used in
         * last 30 seconds */
        if (current - lastCheck > threshold) {
            cache.values().removeIf((cache) -> current - cache.lastUsed > threshold);

            lastCheck = current;
        }
    }

    @Override
    public boolean exists(String name) {
        return getFile(name).exists();
    }

    @Override
    public boolean rename(String id, String newId) {
        File file = getFile(id);

        if (file != null && file.exists()) {
            if (file.renameTo(getFile(newId))) {
                if (MappetConfig.generalDataCaching.get()) {
                    cache.put(newId, cache.remove(id));
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean delete(String name) {
        File file = getFile(name);

        if (file != null && file.delete()) {
            cache.remove(name);

            return true;
        }

        return false;
    }

    @Override
    public Collection<String> getKeys() {
        Set<String> set = new HashSet<>();

        if (folder == null) {
            return set;
        }

        recursiveFind(set, folder, "");

        return set;
    }

    protected void recursiveFind(Set<String> set, File folder, String prefix) {
        File[] files = folder.listFiles();
        if(files == null) return;
        for (File file : files) {
            String name = file.getName();

            if (file.isFile() && name.endsWith(".json")) {
                set.add(prefix + name.replace(".json", ""));
            }
            else if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                if(subFiles == null) continue;
                if (subFiles.length > 0) {
                    recursiveFind(set, file, prefix + name + "/");
                }
                else {
                    set.add(prefix + name + "/");
                }
            }
        }
    }

    protected boolean isData(File file) {
        return file.getName().endsWith(getExtension());
    }

    public File getFile(String name) {
        if (folder == null) {
            return null;
        }

        return new File(folder, name + getExtension());
    }

    public File getFolder() {
        return folder;
    }

    protected String getExtension() {
        return ".json";
    }
}