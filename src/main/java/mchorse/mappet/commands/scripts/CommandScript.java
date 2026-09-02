package mchorse.mappet.commands.scripts;

import mchorse.mappet.commands.MappetSubCommandBase;
import mchorse.mappet.commands.scripts.states.CommandScriptState;
import net.minecraft.command.ICommandSender;

public class CommandScript extends MappetSubCommandBase
{
    public CommandScript()
    {
        add(new CommandScriptState());
        add(new CommandScriptEval());
        add(new CommandScriptExec());
        add(new CommandScriptEngines());
    }

    @Override
    public String getName()
    {
        return "script";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "mappet.commands.mp.script.help";
    }
}