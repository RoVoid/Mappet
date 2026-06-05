package mchorse.mappet.api.states;

import mchorse.mappet.Mappet;
import mchorse.mappet.utils.NBTToJsonLike;

import java.io.File;

public class StatesStorage extends StatesProvider {
    private final File file;

    public StatesStorage(File file) {
        super();
        this.file = file;
    }

    public void load() {
        if (file == null || !file.exists()) return;
        try {
            deserializeNBT(NBTToJsonLike.read(file));
        } catch (Exception e) {
            Mappet.logger.error("Failed to load states", e);
        }
    }

    public boolean save() {
        if (file == null) return false;
        try {
            NBTToJsonLike.write(file, serializeNBT());
        } catch (Exception e) {
            Mappet.logger.error("Failed to save states", e);
            return false;
        }
        return true;
    }

    // without owner
    public static StatesProvider forNpc() {
        return new StatesProvider(new ScriptStates(), null, null, new FactionStates());
    }

    public static StatesProvider forPlayer() {
        return new StatesProvider();
    }
}