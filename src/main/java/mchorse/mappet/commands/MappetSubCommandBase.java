package mchorse.mappet.commands;

import mchorse.mappet.Mappet;
import mchorse.mclib.commands.SubCommandBase;
import mchorse.mclib.commands.utils.L10n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public abstract class MappetSubCommandBase extends SubCommandBase {
    @Override
    public L10n getL10n() {
        return Mappet.l10n;
    }

    @Override
    public String getSyntax() {
        return "{l}{6}/{r}mp {8}" + getName() + "{r} {7}...{r}";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        super.func_184881_a(server, sender, args);
    }
}
