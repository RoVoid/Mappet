package mchorse.mappet.api.conditions.blocks;

import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.api.utils.DataContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StateConditionBlock extends PropertyConditionBlock {
    public StateConditionBlock() {}

    @Override
    public boolean evaluateBlock(DataContext context) {
        ScriptStates states = target.getStates(context).scripts;
        return states != null && (comparison.mode.isString ? states.isString(id) ? compareString(states.getString(id))
                : compareString(String.valueOf(states.getNumber(id))) : compare(states.getNumber(id)));
        // he he
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String name() {
        return comparison.stringify(id);
    }
}