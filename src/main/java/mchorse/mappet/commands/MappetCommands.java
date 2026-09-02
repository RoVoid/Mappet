package mchorse.mappet.commands;

import mchorse.mappet.api.utils.DataContext;
import mchorse.mappet.commands.dialogues.CommandDialogue;
import mchorse.mappet.commands.events.CommandEvent;
import mchorse.mappet.commands.factions.CommandFaction;
import mchorse.mappet.commands.huds.CommandHud;
import mchorse.mappet.commands.morphs.CommandMorph;
import mchorse.mappet.commands.npc.CommandNpc;
import mchorse.mappet.commands.quests.CommandQuest;
import mchorse.mappet.commands.scripts.CommandScript;
import mchorse.mappet.commands.sounds.CommandCustomPlaySound;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class MappetCommands extends MappetSubCommandBase {
    public static DataContext createContext(MinecraftServer server, ICommandSender sender, String argument) throws CommandException {
        return argument.equals("~") ? new DataContext(server) : new DataContext(getEntity(server, sender, argument));
    }

    public MappetCommands() {
        add(new CommandDialogue());
        add(new CommandEvent());
        add(new CommandFaction());
        add(new CommandHud());
        add(new CommandMorph());
        add(new CommandNpc());
        add(new CommandQuest());
        add(new CommandScript());
        add(new CommandCustomPlaySound());
    }

    @Override
    public String getName() {
        return "mp";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "mappet.commands.mp.help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
