package mchorse.mappet.api.states;

import mchorse.mappet.Mappet;
import mchorse.mappet.utils.NBTToJsonLike;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;

public class StateManager {
    public final ScriptStates scripts = new ScriptStates();
    public final DialogueStates dialogues = new DialogueStates();
    public final FactionStates factions = new FactionStates();
    public final QuestStates quests = new QuestStates();

    public final StatesProvider<?> provider = new StatesProvider<>(scripts, dialogues, quests, factions);

    // везде использовать provider <----

    private final File file;

    public StateManager(File file) {
        this.file = file;
    }

    public boolean isGlobal(States states) {
        return states == scripts || states == dialogues || states == factions || states == quests;
    }

    public boolean isGlobal(StatesProvider<?> provider) { return provider == this.provider; }

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

    public NBTTagCompound serializeNBT() {
        return provider.serializeNBT();
    }

    public void deserializeNBT(NBTTagCompound tag) {
        provider.deserializeNBT(tag);
    }
}