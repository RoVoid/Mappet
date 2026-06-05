package mchorse.mappet.api.states;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

// why did I do?
public class StatesProvider implements INBTSerializable<NBTTagCompound> {
    public final ScriptStates scripts;
    public final DialogueStates dialogues;
    public final QuestStates quests;
    public final FactionStates factions;

    public StatesProvider() {
        this(new ScriptStates(), new DialogueStates(), new QuestStates(), new FactionStates());
    }

    public StatesProvider(ScriptStates scripts, DialogueStates dialogues, QuestStates quests, FactionStates factions) {
        this.scripts = scripts;
        this.dialogues = dialogues;
        this.quests = quests;
        this.factions = factions;
    }

    public void bind(Object owner) {
        if (scripts != null) scripts.owner = owner;
        if (dialogues != null) dialogues.owner = owner;
        if (quests != null) quests.owner = owner;
        if (factions != null) factions.owner = owner;
    }

    public boolean owns(States states) {
        return states == scripts || states == dialogues || states == quests || states == factions;
    }

    public boolean is(StatesProvider other) {
        return other == this || other.scripts == scripts && other.dialogues == dialogues && other.quests == quests && other.factions == factions;
    }

    public void from(StatesProvider provider) {
        if (scripts != null && provider.scripts != null) scripts.from(provider.scripts);
        if (dialogues != null && provider.dialogues != null) dialogues.from(provider.dialogues);
        if (quests != null && provider.quests != null) quests.from(provider.quests);
        if (factions != null && provider.factions != null) factions.from(provider.factions);
    }

    public void clear(){
        if (scripts != null) scripts.clear();
        if (dialogues != null) dialogues.clear();
        if (quests != null) quests.clear();
        if (factions != null) factions.clear();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (scripts != null) tag.setTag("Scripts", scripts.serializeNBT());
        if (dialogues != null) tag.setTag("Dialogues", dialogues.serializeNBT());
        if (factions != null) tag.setTag("Factions", factions.serializeNBT());
        if (quests != null) tag.setTag("Quests", quests.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        if (scripts != null && tag.hasKey("Scripts")) scripts.deserializeNBT(tag.getCompoundTag("Scripts"));
        if (dialogues != null && tag.hasKey("Dialogues")) dialogues.deserializeNBT(tag.getCompoundTag("Dialogues"));
        if (factions != null && tag.hasKey("Factions")) factions.deserializeNBT(tag.getCompoundTag("Factions"));
        if (quests != null && tag.hasKey("Quests")) quests.deserializeNBT(tag.getCompoundTag("Quests"));
    }
}
