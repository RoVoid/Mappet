package mchorse.mappet.commands.states;

import mchorse.mappet.api.states.States;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class CommandStatePrint extends CommandStateBase {
    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "mappet.commands.mp.state.print";
    }

    @Override
    public String getSyntax() {
        return "{l}{6}/{r}mp {8}state print{r} {7}<target>{r}";
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int getRequiredArgs() {
        return 1;
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        States states = CommandState.getStates(server, sender, args[0]);
        ITextComponent component = getL10n().info("states.print", args[0]);

        for (String key : states.keys()) {
            Object value = states.values().get(key);
            component.appendSibling(new TextComponentString(key + " " + (value instanceof String ? "(s)" : "(n)") + " §7=§r " + value + "\n"));
        }

        if (!states.values().isEmpty()) sender.sendMessage(component);
    }
}