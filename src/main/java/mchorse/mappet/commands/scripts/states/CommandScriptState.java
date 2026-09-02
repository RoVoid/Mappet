package mchorse.mappet.commands.scripts.states;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.api.states.StatesProvider;
import mchorse.mappet.commands.MappetSubCommandBase;
import mchorse.mappet.utils.EntityUtils;
import mchorse.mappet.utils.ServerUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandScriptState extends MappetSubCommandBase {
    public static ScriptStates getStates(MinecraftServer server, ICommandSender sender, String target) throws CommandException {
        if (ServerUtils.isServer(target)) return Mappet.states.scripts;
        StatesProvider provider = EntityUtils.getStates(getEntity(server, sender, target));
        if (provider == null) throw new CommandException("states.invalid_target", target);
        return provider.scripts;
    }

    public CommandScriptState() {
        add(new CommandScriptStateAdd());
        add(new CommandScriptStateClear());
        add(new CommandScriptStateSet());
    }

    @Override
    public String getName() {
        return "state";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "mappet.commands.mp.state.help";
    }
}
