package mchorse.mappet.events.handlers;

import mchorse.mappet.api.quests.Quest;
import mchorse.mappet.api.quests.Quests;
import mchorse.mappet.api.states.States;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.events.StateChangedEvent;
import mchorse.mappet.Mappet;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.quests.PacketQuest;
import mchorse.mappet.network.packets.quests.PacketQuests;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class QuestTickHandler {

    private final Set<EntityPlayer> playersToCheck = new HashSet<>();

    public void reset() {
        playersToCheck.clear();
    }

    public void markForCheck(EntityPlayer player) {
        playersToCheck.add(player);
    }

    @SubscribeEvent
    public void onPlayerOpenOrCloseContainer(PlayerContainerEvent event) {
        markForCheck(event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onPlayerPickUp(EntityItemPickupEvent event) {
        markForCheck(event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ICharacter character = Character.get(player);
        if (character != null) {
            for (Quest quest : character.getQuests().quests.values()) {
                quest.mobWasKilled(player, event.getEntity());
            }
            markForCheck(player);
        }
    }

    @SubscribeEvent
    public void onStateChange(StateChangedEvent event) {
        if (event.type != States.Type.QUEST) return;

        boolean isGlobal = Mappet.states.owns(event.states);

        for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            ICharacter character = Character.get(player);

            if (character != null && (isGlobal || character.getStates().owns(event.states))) {
                boolean changed = false;
                for (Quest quest : character.getQuests().quests.values()) {
                    changed |= quest.stateWasUpdated(player);
                }
                if (changed) markForCheck(player);
            }
        }
    }

    /**
     * Send a player's active quests to their client — used on login and
     * on respawn.
     */
    public void syncQuests(EntityPlayerMP player, ICharacter character) {
        if (character.getQuests().quests.isEmpty()) return;

        character.getQuests().initiate(player);
        Dispatcher.sendTo(new PacketQuests(character.getQuests()), player);
    }

    /**
     * Re-check every marked player's quests, complete/reward the ones
     * that are done, and push updates for the rest.
     */
    public void tick() {
        for (EntityPlayer player : playersToCheck) {
            ICharacter character = Character.get(player);
            if (character == null) continue;

            Quests quests = character.getQuests();
            Iterator<Map.Entry<String, Quest>> it = quests.quests.entrySet().iterator();

            quests.iterating = true;
            while (it.hasNext()) {
                Map.Entry<String, Quest> entry = it.next();
                Quest quest = entry.getValue();

                if (quest.instant && quest.rewardIfComplete(player)) {
                    it.remove();
                    Dispatcher.sendTo(new PacketQuest(entry.getKey(), null), (EntityPlayerMP) player);
                }
                else Dispatcher.sendTo(new PacketQuest(entry.getKey(), entry.getValue()), (EntityPlayerMP) player);
            }
            quests.flush(player);
        }

        playersToCheck.clear();
    }
}
