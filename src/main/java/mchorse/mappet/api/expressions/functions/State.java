package mchorse.mappet.api.expressions.functions;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.States;
import mchorse.mappet.utils.EntityUtils;
import mchorse.mclib.math.IValue;
import mchorse.mclib.math.functions.Function;
import net.minecraft.command.CommandBase;

public class State extends Function {
    /**
     * Get states repository out of given target
     */
    public static States getState(String target) {
        States states = null;

        if (target.equals("~")) states = Mappet.states;
        else try {
            states = EntityUtils.getStates(CommandBase.getEntity(Mappet.expressions.getServer(), Mappet.expressions.getServer(), target));
        } catch (Exception ignored) {
        }

        return states;
    }

    public State(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    public Object getValue() {
        String target = args.length > 1 ? getArg(1).stringValue() : "~";
        States states = getState(target);
        return states == null ? null : states.values().get(getArg(0).stringValue());
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public IValue get() {
        if (isNumber()) result.set(doubleValue());
        else result.set(stringValue());

        return result;
    }

    @Override
    public boolean isNumber() {
        return !(getValue() instanceof String);
    }

    @Override
    public double doubleValue() {
        Object value = getValue();
        return value instanceof Number ? ((Number) value).doubleValue() : 0;
    }

    @Override
    public boolean booleanValue() {
        return getValue() != null;
    }

    @Override
    public String stringValue() {
        Object value = getValue();
        return value instanceof String ? (String) value : "";
    }
}