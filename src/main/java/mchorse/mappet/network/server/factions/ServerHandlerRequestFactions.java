package mchorse.mappet.network.server.factions;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.factions.Faction;
import mchorse.mappet.api.states.FactionStates;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.factions.PacketFactions;
import mchorse.mappet.network.packets.factions.PacketRequestFactions;
import mchorse.mclib.network.ServerMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Map;

public class ServerHandlerRequestFactions extends ServerMessageHandler<PacketRequestFactions> {
    public static void collectFactions(EntityPlayerMP player, FactionStates states) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Faction> factions = new HashMap<>();

        for (Map.Entry<String, Object> entry : states.values().entrySet()) {
            if (!(entry.getValue() instanceof Number)) continue;

            Faction faction = Mappet.factions.load(entry.getKey());
            if (faction == null || !faction.isVisible(player)) continue;

            factions.put(entry.getKey(), faction);
            scores.put(entry.getKey(), ((Number) entry.getValue()).doubleValue());
        }

        if (!factions.isEmpty()) Dispatcher.sendTo(new PacketFactions(factions, scores), player);
    }

    @Override
    public void run(EntityPlayerMP player, PacketRequestFactions message) {
        ICharacter character = Character.get(player);
        if (character != null) collectFactions(player, character.getStates().factions);
    }
}