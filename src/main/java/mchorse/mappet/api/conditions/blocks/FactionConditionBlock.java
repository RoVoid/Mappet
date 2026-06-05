package mchorse.mappet.api.conditions.blocks;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.factions.Faction;
import mchorse.mappet.api.factions.FactionAttitude;
import mchorse.mappet.api.states.FactionStates;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.api.utils.TargetMode;
import mchorse.mappet.utils.EnumUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FactionConditionBlock extends PropertyConditionBlock {
    public FactionCheck faction = FactionCheck.SCORE;

    public FactionConditionBlock() {
        super();
    }

    @Override
    protected TargetMode getDefaultTarget() {
        return TargetMode.SUBJECT;
    }

    @Override
    public boolean evaluateBlock(DataContext context) {
        if (target.mode == TargetMode.GLOBAL) return false;

        FactionStates states = target.getStates(context).factions;
        if (states == null) return false;

        if (faction == FactionCheck.SCORE) {
            if (!states.has(id)) return false;
            if (comparison.mode.isString) return compareString(String.valueOf(states.get(id)));
            return compare(states.get(id));
        }

        Faction faction = Mappet.factions.load(id);
        return faction != null && faction.get(states) == this.faction.attitude;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String name() {
        switch (faction) {
            case SCORE:
                return comparison.stringify(id);
            case AGGRESSIVE:
                return I18n.format("mappet.gui.conditions.faction.is_aggressive", id);
            case PASSIVE:
                return I18n.format("mappet.gui.conditions.faction.is_passive", id);
            default:
                return I18n.format("mappet.gui.conditions.faction.is_friendly", id);
        }
    }

    @Override
    public void serializeNBT(NBTTagCompound tag) {
        super.serializeNBT(tag);

        tag.setInteger("Faction", faction.ordinal());
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        super.deserializeNBT(tag);

        faction = EnumUtils.getValue(tag.getInteger("Faction"), FactionCheck.values(), FactionCheck.SCORE);
    }

    public enum FactionCheck {
        AGGRESSIVE(FactionAttitude.AGGRESSIVE), PASSIVE(FactionAttitude.PASSIVE), FRIENDLY(FactionAttitude.FRIENDLY), SCORE(null);

        public final FactionAttitude attitude;
        FactionCheck(FactionAttitude attitude) {
            this.attitude = attitude;
        }
    }
}