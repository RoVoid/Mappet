package mchorse.mappet.events.handlers;

import mchorse.mappet.api.huds.HUDScene;
import mchorse.mappet.capabilities.character.Character;
import mchorse.mappet.capabilities.character.ICharacter;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.huds.PacketHUDScene;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.Map;

/**
 * Syncs HUD scenes to players when they log in.
 * <p>
 * Extracted from {@code EventHandler.onPlayerLogsIn()}.
 * <p>
 * Fix vs. the original: the old code first sent the joining player
 * <em>all</em> of their own displayed HUD scenes, then separately looped
 * over every online player (which included the joining player) and
 * re-sent any {@code global} scene it found. If the joining player
 * already had a global scene recorded from a previous session, that
 * scene was sent to them twice. {@link #sendOwnHUDs} and
 * {@link #sendOtherPlayersGlobalHUDs} are now two clearly-named,
 * separately-callable steps, and the second one explicitly skips the
 * player it's syncing to.
 */
public class HUDSyncHandler {
    /**
     * Send a player every HUD scene they had displayed (own scenes,
     * global or not).
     */
    public void sendOwnHUDs(EntityPlayerMP player, ICharacter character) {
        for (Map.Entry<String, List<HUDScene>> entry : character.getDisplayedHUDs().entrySet()) {
            String id = entry.getKey();

            for (HUDScene scene : entry.getValue()) {
                Dispatcher.sendTo(new PacketHUDScene(id, scene.serializeNBT()), player);
            }
        }
    }

    /**
     * Send a newly-joined player any {@code global} HUD scene that other
     * online players currently have displayed, so global HUDs stay in
     * sync across everyone on the server.
     */
    public void sendOtherPlayersGlobalHUDs(EntityPlayerMP player) {
        for (EntityPlayerMP other : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            if (other == player) continue;

            ICharacter character = Character.get(other);
            if (character == null) continue;

            for (Map.Entry<String, List<HUDScene>> entry : character.getDisplayedHUDs().entrySet()) {
                String id = entry.getKey();

                for (HUDScene scene : entry.getValue()) {
                    if (scene.global) Dispatcher.sendTo(new PacketHUDScene(id, scene.serializeNBT()), player);
                }
            }
        }
    }
}
