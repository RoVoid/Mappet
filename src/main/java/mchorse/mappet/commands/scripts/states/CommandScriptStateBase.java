package mchorse.mappet.commands.scripts.states;

import mchorse.mappet.api.states.ScriptStates;
import mchorse.mappet.api.states.States;
import mchorse.mappet.commands.MappetCommandBase;
import mchorse.mappet.utils.ServerUtils;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public abstract class CommandScriptStateBase extends MappetCommandBase {
    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return args.length > 0 && !args[0].equals("~") && index == 0;
    }

    @Override
    public int getRequiredArgs() {
        return 3;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, ServerUtils.playerNamesAndServer(server));

        if (args.length == 2) try {
            ScriptStates states = CommandScriptState.getStates(server, sender, args[0]);
            return getListOfStringsMatchingLastWord(args, states.keys());
        } catch (Exception ignored) {
        }

        return super.getTabCompletions(server, sender, args, targetPos);
    }
}