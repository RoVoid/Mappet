package mchorse.mappet.utils;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.states.StatesProvider;
import mchorse.mappet.commands.factions.CommandFaction;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Arrays;
import java.util.List;

// rename to Utils
public class ServerUtils {
    // rename to server
    public static MinecraftServer instance(){
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    public static boolean isServer(String target){
        return target != null && target.equals("~");
    }

    public static List<String> playerNames(MinecraftServer server){
        return Arrays.asList(server.getPlayerList().getOnlinePlayerNames());
    }

    public static List<String> playerNamesAndServer(MinecraftServer server){
        List<String> list = playerNames(server);
        list.add("~");
        return list;
    }
}
