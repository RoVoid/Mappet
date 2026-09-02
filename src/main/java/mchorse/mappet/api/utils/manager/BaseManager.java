package mchorse.mappet.api.utils.manager;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.utils.AbstractData;
import mchorse.mappet.config.MappetConfig;
import mchorse.mappet.utils.NBTToJsonLike;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BaseManager<T extends AbstractData> implements IManager<T> {
    protected Map<String, ManagerCache> cache = new HashMap<>();
    protected File root;
    protected long lastCheck;

    public BaseManager(File folder) {
        if (folder == null) return;
        root = folder;
        root.mkdirs();
    }

    /* Cache */

    protected void doExpirationCheck() {
        final int threshold = 1000 * 30; // 30 sec
        long current = System.currentTimeMillis();

        if (current - lastCheck > threshold) {
            cache.values().removeIf((cache) -> current - cache.lastUsed > threshold);
            lastCheck = current;
        }
    }

    protected NBTTagCompound getCached(String id) throws Exception {
        File file = getFile(id);

        if (!MappetConfig.generalDataCaching.get()) return NBTToJsonLike.read(file); // !isCaching

        long lastUpdated = file.lastModified();
        ManagerCache cache = this.cache.get(id);

        if (cache != null) {
            if (cache.lastUpdated < lastUpdated) this.cache.remove(id);
            else {
                cache.update();
                doExpirationCheck();
                return cache.tag;
            }
        }

        NBTTagCompound tag = NBTToJsonLike.read(file);
        this.cache.put(id, new ManagerCache(tag, lastUpdated));
        return tag;
    }

    /* IManager */

    @Override
    public final T create(String id, NBTTagCompound tag) {
        T data = createData(id, tag);
        data.setId(id);
        return data;
    }

    protected abstract T createData(String id, NBTTagCompound tag);

    @Override
    public T load(String id) {
        try {
            return create(id, getCached(id));
        } catch (Exception e) {
            Mappet.logger.error("Failed to load '" + id + "': " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean exists(String id) {
        File file = getFile(id);
        return file != null && file.exists();
    }

    @Override
    public boolean rename(String id, String newId) {
        File file = getFile(id);
        if (file == null || !file.exists() || !file.renameTo(getFile(newId))) return false;
        if (MappetConfig.generalDataCaching.get()) cache.put(newId, cache.remove(id));
        return true;
    }

    @Override
    public boolean delete(String id) {
        File file = getFile(id);
        if (file == null || !file.delete()) return false;
        cache.remove(id);
        return true;
    }

    public boolean save(String id, T data) {
        return save(id, data.serializeNBT());
    }

    @Override
    public boolean save(String id, NBTTagCompound tag) {
        try {
            NBTToJsonLike.write(getFile(id), tag);
            cache.remove(id);
            return true;
        } catch (Exception e) {
            Mappet.logger.error("Failed to save '" + id + "': " + e.getMessage());
        }
        return false;
    }

    @Override
    public Set<String> getPaths() { // with folders
        Set<String> set = new HashSet<>();
        if (root != null) recursiveFind(set, root, "", true);
        return set;
    }

    @Override
    public Set<String> getIDs() { // without folders
        Set<String> set = new HashSet<>();
        if (root != null) recursiveFind(set, root, "", false);
        return set;
    }

    /* File helpers */

    protected void recursiveFind(Set<String> set, File folder, String prefix, boolean withFolders) {
        if (!prefix.isEmpty() && withFolders) set.add(prefix);

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            String name = file.getName();
            if (file.isFile() && name.endsWith(getExtension())) set.add(prefix + name.substring(0, name.length() - getExtension().length()));
            else if (file.isDirectory()) recursiveFind(set, file, prefix + name + '/', withFolders);
        }
    }

    public File getFile(String name) {
        return root == null || name == null || name.isEmpty() || name.charAt(0) == '.' ? null : new File(root, name + getExtension());
    }

    public static boolean isFolder(String path){
        return path != null && path.endsWith("/"); // small util
    }

    @Override
    public void addFolder(String path) {
        if (root == null || path == null || path.isEmpty()) return;
        new File(root, path).mkdirs();
    }

    @Override
    public void renameFolder(String oldPath, String newPath) {
        if (root == null || oldPath == null || newPath == null) return;
        new File(root, oldPath).renameTo(new File(root, newPath));
        String prefix = oldPath.endsWith("/") ? oldPath : oldPath + "/";
        cache.keySet().removeIf(id -> id.startsWith(prefix));
    }

    @Override
    public void deleteFolder(String path) {
        if (root == null || path == null || path.isEmpty()) return;
        String prefix = path.endsWith("/") ? path : path + "/";
        cache.keySet().removeIf(id -> id.startsWith(prefix));
        deleteRecursive(new File(root, path));
    }

    private void deleteRecursive(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursive(child);
        }
        file.delete();
    }

    @Override
    public File getFolder() {return root;}

    // don't forget '.' at beginning of extension
    @Override
    public String getExtension() {return ".json";}
}