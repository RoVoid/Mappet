package mchorse.mappet.commands.states;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.States;
import mchorse.mappet.api.utils.DataContext;
import mchorse.mclib.commands.SubCommandBase;
import mchorse.mclib.math.IValue;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandStateIf extends CommandStateBase {
    @Override
    public String getName() {
        return "if";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "mappet.commands.mp.state.if";
    }

    @Override
    public String getSyntax() {
        return "{l}{6}/{r}mp {8}state if{r} {7}<target> <id> <expression>{r}";
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        States states = CommandState.getStates(server, sender, args[0]);
        String id = args[1];

        if (!states.has(id)) throw new CommandException("states.missing", id);

        Object previous = states.values().get(id);

        String expression = String.join(" ", SubCommandBase.dropFirstArguments(args, 2));
        DataContext context;

        if (sender instanceof EntityPlayer) context = new DataContext((EntityPlayer) sender);
        else context = new DataContext(server);

        if (previous instanceof Number) context.set("value", ((Number) previous).doubleValue());
        else if (previous instanceof String) context.set("value", (String) previous);

        IValue result = Mappet.expressions.set(context).parse(expression);

        if (!result.booleanValue()) throw new CommandException("states.false", id);

        getL10n().info(sender, "states.true", id);
    }
}