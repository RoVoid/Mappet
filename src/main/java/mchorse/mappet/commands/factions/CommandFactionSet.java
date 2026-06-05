package mchorse.mappet.commands.factions;

import mchorse.mappet.api.factions.Faction;
import mchorse.mappet.api.states.FactionStates;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandFactionSet extends CommandFactionBase {
    @Override
    public String getName() {
        return "set";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "mappet.commands.mp.faction.set";
    }

    @Override
    public String getSyntax() {
        return "{l}{6}/{r}mp {8}faction set{r} {7}<target> <id> <expression>{r}";
    }

    @Override
    public void executeCommand(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String id = args[1];
        Faction faction = getFaction(id);
        FactionStates states = CommandFaction.getStates(server, sender, args[0]);
        if (states == null) return;

        int value = CommandBase.parseInt(args[2]);
        states.set(id, value);
        getL10n().info(sender, "factions.set", id, states.get(id));
    }
}