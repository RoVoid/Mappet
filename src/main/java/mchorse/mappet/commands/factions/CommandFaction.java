package mchorse.mappet.commands.factions;

import mchorse.mappet.api.states.FactionStates;
import mchorse.mappet.api.states.StatesProvider;
import mchorse.mappet.commands.MappetSubCommandBase;
import mchorse.mappet.utils.EntityUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

public class CommandFaction extends MappetSubCommandBase
{
    public static FactionStates getStates(MinecraftServer server, ICommandSender sender, String target) throws CommandException
    {
        StatesProvider provider = EntityUtils.getStates(getEntity(server, sender, target));
        if (provider == null) throw new CommandException("states.invalid_target", target);
        return provider.factions;
    }

    public CommandFaction()
    {
        add(new CommandFactionAdd());
        add(new CommandFactionClear());
        add(new CommandFactionSet());
    }

    @Override
    public String getName()
    {
        return "faction";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "mappet.commands.mp.faction.help";
    }
}