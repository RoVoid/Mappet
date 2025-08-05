package mchorse.mappet.utils;

import com.google.common.base.Predicate;
import mchorse.mappet.api.states.States;
import mchorse.mappet.entities.EntityNpc;
import mchorse.mclib.math.IValue;
import mchorse.mclib.math.MathBuilder;
import mchorse.mclib.math.Variable;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.IEntitySelectorFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MappetNpcSelector implements IEntitySelectorFactory {
    public static final String ARGUMENT_MAPPET_NPC_ID = "mpid";
    public static final String ARGUMENT_MAPPET_STATES = "mpe";

    public static final SelectorMathBuilder BUILDER = new SelectorMathBuilder();

    @Nonnull
    @Override
    public List<Predicate<Entity>> createPredicates(Map<String, String> arguments, String mainSelector, ICommandSender sender, Vec3d position) {
        List<Predicate<Entity>> list = new ArrayList<>();

        if (arguments.containsKey(ARGUMENT_MAPPET_NPC_ID))
            addNpcIdPredicate(list, arguments.get(ARGUMENT_MAPPET_NPC_ID));

        if (arguments.containsKey(ARGUMENT_MAPPET_STATES))
            addStatesPredicate(list, arguments.get(ARGUMENT_MAPPET_STATES));

        return list;
    }

    private void addNpcIdPredicate(List<Predicate<Entity>> list, String id) {
        boolean negative = id.startsWith("!");

        if (negative) id = id.substring(1);

        final String finalId = id;

        list.add((e) -> {
            if (e instanceof EntityNpc) {
                String npcId = ((EntityNpc) e).getId();

                return negative != npcId.equals(finalId);
            }

            return false;
        });
    }

    private void addStatesPredicate(List<Predicate<Entity>> list, String expression) {
        BUILDER.reset();

        try {
            IValue value = BUILDER.parse(expression);

            list.add((e) -> {
                States states = EntityUtils.getStates(e);

                if (states == null) return false;

                for (Variable variable : BUILDER.variables.values()) {
                    Object v = states.values().get(variable.getName());

                    if (v != null) {
                        if (v instanceof String) variable.set((String) v);
                        else if (v instanceof Number) variable.set(((Number) v).doubleValue());
                    }
                    else variable.set(0);
                }

                return value.booleanValue();
            });
        } catch (Exception ignored) {
        }
    }

    /**
     * A little hackaroony to avoid having extra symbols in expressions
     */
    public static class SelectorMathBuilder extends MathBuilder {
        public void reset() {
            variables.clear();
        }

        @Override
        protected Variable getVariable(String name) {
            /* For any string-like literal this creates a variable and later
             * substituted to corresponding state's value */
            Variable variable = super.getVariable(name);

            if (variable == null) {
                variable = new Variable(name, 0);
                variables.put(name, variable);
            }

            return variable;
        }
    }
}