package mchorse.mappet.api.states;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

// why did I do?
public class StatesProvider<T> implements INBTSerializable<NBTTagCompound> {
    public final ScriptStates scripts;
    public final DialogueStates dialogues;
    public final QuestStates quests;
    public final FactionStates factions;

    public T owner = null;

    public StatesProvider() {
        this(new ScriptStates(), new DialogueStates(), new QuestStates(), new FactionStates());
    }

    public StatesProvider(ScriptStates scripts, DialogueStates dialogues, QuestStates quests, FactionStates factions) {
        this.scripts = scripts;
        this.dialogues = dialogues;
        this.quests = quests;
        this.factions = factions;
    }

    public StatesProvider<T> bind(T owner) {
        this.owner = owner;
        return this;
    }

    public void from(StatesProvider<?> provider) {
        scripts.from(provider.scripts);
        dialogues.from(provider.dialogues);
        quests.from(provider.quests);
        factions.from(provider.factions);
    }

    public void to(StatesProvider<?> provider){
        provider.scripts.from(scripts);
        provider.dialogues.from(dialogues);
        provider.quests.from(quests);
        provider.factions.from(factions);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("scripts", scripts.serializeNBT());
        tag.setTag("dialogues", dialogues.serializeNBT());
        tag.setTag("factions", factions.serializeNBT());
        tag.setTag("quests", quests.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        scripts.deserializeNBT(tag.getCompoundTag("scripts"));
        dialogues.deserializeNBT(tag.getCompoundTag("dialogues"));
        factions.deserializeNBT(tag.getCompoundTag("factions"));
        quests.deserializeNBT(tag.getCompoundTag("quests"));
    }
}
