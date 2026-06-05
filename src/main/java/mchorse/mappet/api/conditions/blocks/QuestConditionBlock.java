package mchorse.mappet.api.conditions.blocks;

import mchorse.mappet.api.states.QuestStates;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.utils.EnumUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class QuestConditionBlock extends TargetConditionBlock {
    public QuestCheck quest = QuestCheck.COMPLETED;

    @Override
    public boolean evaluateBlock(DataContext context) {
        if (target.mode == TargetMode.GLOBAL) {
            QuestStates states = target.getStates(context).quests;

            if (quest == QuestCheck.ABSENT) return !states.wasCompleted(id) && hasServerInProgress(context);
            if (quest == QuestCheck.PRESENT) return hasServerInProgress(context);
            return states.wasCompleted(id);
        }
        ICharacter character = target.getCharacter(context);

        if (character == null) return false;

        if (quest == QuestCheck.ABSENT) return !character.getStates().quests.wasCompleted(id) && !character.getQuests().has(id);
        if (quest == QuestCheck.PRESENT) return character.getQuests().has(id);
        return character.getStates().quests.wasCompleted(id);
    }

    private boolean hasServerInProgress(DataContext context) {
        for (EntityPlayer player : context.server.getPlayerList().getPlayers()) {
            Character character = Character.get(player);
            if (character != null && character.getQuests().has(id)) return true;
        }
        return false;
    }

    @Override
    protected TargetMode getDefaultTarget() {
        return TargetMode.SUBJECT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String name() {
        if (quest == QuestCheck.ABSENT) return I18n.format("mappet.gui.conditions.quest.is_absent", id);
        if (quest == QuestCheck.PRESENT) return I18n.format("mappet.gui.conditions.quest.is_present", id);

        return I18n.format("mappet.gui.conditions.quest.is_completed", id);
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);

        tag.setInteger("Quest", quest.ordinal());
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);

        quest = EnumUtils.getValue(tag.getInteger("Quest"), QuestCheck.values(), QuestCheck.COMPLETED);
    }

    public enum QuestCheck {
        ABSENT, PRESENT, COMPLETED
    }
}