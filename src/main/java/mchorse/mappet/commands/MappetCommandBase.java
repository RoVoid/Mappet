package mchorse.mappet.commands;

import mchorse.mappet.Mappet;
import mchorse.mclib.commands.McCommandBase;
import mchorse.mclib.commands.utils.L10n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public abstract class MappetCommandBase extends McCommandBase {
    @Override
    public L10n getL10n() {
        return Mappet.l10n;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        super.func_184881_a(server, sender, args);
    }
}
