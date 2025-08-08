package mchorse.mappet.api.data;

import mchorse.mappet.api.utils.manager.BaseManager;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DataManager extends BaseManager<Data> {
    private File date;
    private Instant lastClear;
    private boolean lastInventory;

    public DataManager(File folder) {
        super(folder);

        if (folder != null) {
            date = new File(folder.getParentFile(), "date.txt");
        }
    }

    public Instant getLastClear() {
        if (lastClear == null) {
            try {
                String text = FileUtils.readFileToString(date, StandardCharsets.UTF_8).trim();
                String[] splits = text.split("\n");

                lastClear = Instant.parse(splits[0]);

                if (splits.length > 1) {
                    lastInventory = splits[1].trim().equals("1");
                }
            } catch (Exception e) {
                lastClear = Instant.EPOCH;
            }
        }

        return lastClear;
    }

    public boolean getLastInventory() {
        if (lastClear == null) {
            getLastClear();
        }

        return lastInventory;
    }

    public void updateLastClear(boolean inventory) {
        lastClear = Instant.now();

        try {
            FileUtils.writeStringToFile(date, lastClear.toString() + (inventory ? "\n1" : "\n0"), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected Data createData(String id, NBTTagCompound tag) {
        Data data = new Data();

        if (tag != null) {
            data.deserializeNBT(tag);
        }

        return data;
    }
}