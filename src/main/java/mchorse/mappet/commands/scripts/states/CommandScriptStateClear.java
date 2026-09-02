package mchorse.mappet.commands.scripts.states;

import mchorse.mappet.api.states.ScriptStates;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandScriptStateClear extends CommandScriptStateBase {
    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "mappet.commands.mp.state.clear";
    }

    @Override
    public String getSyntax() {
        return "{l}{6}/{r}mp {8}state clear{r} {7}<target> [id]{r}";
    }

    @Override
    public int getRequiredArgs() {
        return 1;
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ScriptStates states = CommandScriptState.getStates(server, sender, args[0]);
        if (args.length > 1) {
            String id = args[1];
            states.remove(id);
            getL10n().info(sender, "states.clear", id);
        }
        else {
            states.clear();
            getL10n().info(sender, "states.clear_all", args[0]);
        }
    }
}