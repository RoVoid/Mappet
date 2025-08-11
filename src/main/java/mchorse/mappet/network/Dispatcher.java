package mchorse.mappet.network;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.client.ClientHandlerBlackAndWhiteShader;
import mchorse.mappet.network.client.ClientHandlerLockPerspective;
import mchorse.mappet.network.client.ClientHandlerPack;
import mchorse.mappet.network.client.ClientHandlerSyncHotkeys;
import mchorse.mappet.network.client.blocks.ClientHandlerEditConditionModel;
import mchorse.mappet.network.client.blocks.ClientHandlerEditEmitter;
import mchorse.mappet.network.client.blocks.ClientHandlerEditRegion;
import mchorse.mappet.network.client.blocks.ClientHandlerEditTrigger;
import mchorse.mappet.network.client.content.ClientHandlerContentData;
import mchorse.mappet.network.client.content.ClientHandlerContentNames;
import mchorse.mappet.network.client.content.ClientHandlerServerSettings;
import mchorse.mappet.network.client.content.ClientHandlerStates;
import mchorse.mappet.network.client.dialogue.ClientHandlerDialogueFragment;
import mchorse.mappet.network.client.huds.ClientHandlerHUDMorph;
import mchorse.mappet.network.client.huds.ClientHandlerHUDScene;
import mchorse.mappet.network.client.items.ClientHandlerScriptedItemInfo;
import mchorse.mappet.network.client.logs.ClientHandlerLogs;
import mchorse.mappet.network.client.npc.ClientHandlerNpcList;
import mchorse.mappet.network.client.npc.ClientHandlerNpcState;
import mchorse.mappet.network.client.npc.ClientHandlerNpcStateChange;
import mchorse.mappet.network.client.quests.ClientHandlerQuest;
import mchorse.mappet.network.client.quests.ClientHandlerQuests;
import mchorse.mappet.network.client.scripts.*;
import mchorse.mappet.network.client.ui.ClientHandlerCloseUI;
import mchorse.mappet.network.client.ui.ClientHandlerUI;
import mchorse.mappet.network.client.ui.ClientHandlerUIData;
import mchorse.mappet.network.client.utils.ClientHandlerChangedBoundingBox;
import mchorse.mappet.network.common.PacketBlackAndWhiteShader;
import mchorse.mappet.network.common.PacketLockPerspective;
import mchorse.mappet.network.common.PacketPack;
import mchorse.mappet.network.common.blocks.PacketEditConditionModel;
import mchorse.mappet.network.common.blocks.PacketEditEmitter;
import mchorse.mappet.network.common.blocks.PacketEditRegion;
import mchorse.mappet.network.common.blocks.PacketEditTrigger;
import mchorse.mappet.network.common.content.*;
import mchorse.mappet.network.common.dialogue.PacketDialogueFragment;
import mchorse.mappet.network.common.dialogue.PacketFinishDialogue;
import mchorse.mappet.network.common.dialogue.PacketPickReply;
import mchorse.mappet.network.common.factions.PacketRequestFactions;
import mchorse.mappet.network.common.hotkey.PacketSyncHotkeys;
import mchorse.mappet.network.common.hotkey.PacketTriggeredHotkeys;
import mchorse.mappet.network.common.huds.PacketHUDMorph;
import mchorse.mappet.network.common.huds.PacketHUDScene;
import mchorse.mappet.network.common.items.PacketScriptedItemInfo;
import mchorse.mappet.network.common.logs.PacketLogs;
import mchorse.mappet.network.common.logs.PacketRequestLogs;
import mchorse.mappet.network.common.npc.*;
import mchorse.mappet.network.common.quests.PacketQuest;
import mchorse.mappet.network.common.quests.PacketQuestAction;
import mchorse.mappet.network.common.quests.PacketQuestVisibility;
import mchorse.mappet.network.common.quests.PacketQuests;
import mchorse.mappet.network.common.scripts.*;
import mchorse.mappet.network.common.ui.PacketCloseUI;
import mchorse.mappet.network.common.ui.PacketUI;
import mchorse.mappet.network.common.ui.PacketUIData;
import mchorse.mappet.network.common.utils.PacketChangedBoundingBox;
import mchorse.mappet.network.server.ServerHandlerHotkeys;
import mchorse.mappet.network.server.blocks.ServerHandlerEditConditionModel;
import mchorse.mappet.network.server.blocks.ServerHandlerEditEmitter;
import mchorse.mappet.network.server.blocks.ServerHandlerEditRegion;
import mchorse.mappet.network.server.blocks.ServerHandlerEditTrigger;
import mchorse.mappet.network.server.content.*;
import mchorse.mappet.network.server.dialogue.ServerHandlerFinishDialogue;
import mchorse.mappet.network.server.dialogue.ServerHandlerPickReply;
import mchorse.mappet.network.server.factions.ServerHandlerRequestFactions;
import mchorse.mappet.network.server.items.ServerHandlerScriptedItemInfo;
import mchorse.mappet.network.server.logs.ServerHandlerLogs;
import mchorse.mappet.network.server.npc.ServerHandlerNpcJump;
import mchorse.mappet.network.server.npc.ServerHandlerNpcList;
import mchorse.mappet.network.server.npc.ServerHandlerNpcState;
import mchorse.mappet.network.server.npc.ServerHandlerNpcTool;
import mchorse.mappet.network.server.quests.ServerHandlerQuestAction;
import mchorse.mappet.network.server.quests.ServerHandlerQuestVisibility;
import mchorse.mappet.network.server.scripts.ServerClientSettingsHandler;
import mchorse.mappet.network.server.scripts.ServerHandlerClick;
import mchorse.mappet.network.server.scripts.ServerHandlerRepl;
import mchorse.mappet.network.server.ui.ServerHandlerUI;
import mchorse.mappet.network.server.ui.ServerHandlerUIData;
import mchorse.mclib.network.AbstractDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Network dispatcher
 */
public class Dispatcher {
    public static final AbstractDispatcher DISPATCHER = new AbstractDispatcher(Mappet.MOD_ID) {
        @Override
        public void register() {
            /* Dialogue */
            register(PacketDialogueFragment.class, ClientHandlerDialogueFragment.class, Side.CLIENT);
            register(PacketPickReply.class, ServerHandlerPickReply.class, Side.SERVER);
            register(PacketFinishDialogue.class, ServerHandlerFinishDialogue.class, Side.SERVER);

            /* Blocks */
            register(PacketEditEmitter.class, ClientHandlerEditEmitter.class, Side.CLIENT);
            register(PacketEditEmitter.class, ServerHandlerEditEmitter.class, Side.SERVER);

            register(PacketEditTrigger.class, ClientHandlerEditTrigger.class, Side.CLIENT);
            register(PacketEditTrigger.class, ServerHandlerEditTrigger.class, Side.SERVER);

            register(PacketEditRegion.class, ClientHandlerEditRegion.class, Side.CLIENT);
            register(PacketEditRegion.class, ServerHandlerEditRegion.class, Side.SERVER);

            register(PacketEditConditionModel.class, ClientHandlerEditConditionModel.class, Side.CLIENT);
            register(PacketEditConditionModel.class, ServerHandlerEditConditionModel.class, Side.SERVER);

            /* Scripted item */
            register(PacketScriptedItemInfo.class, ClientHandlerScriptedItemInfo.class, Side.CLIENT);
            register(PacketScriptedItemInfo.class, ServerHandlerScriptedItemInfo.class, Side.SERVER);

            /* Creative editing */
            register(PacketContentRequestNames.class, ServerHandlerContentRequestNames.class, Side.SERVER);
            register(PacketContentRequestData.class, ServerHandlerContentRequestData.class, Side.SERVER);
            register(PacketContentData.class, ClientHandlerContentData.class, Side.CLIENT);
            register(PacketContentData.class, ServerHandlerContentData.class, Side.SERVER);
            register(PacketContentFolder.class, ServerHandlerContentFolder.class, Side.SERVER);
            register(PacketContentNames.class, ClientHandlerContentNames.class, Side.CLIENT);
            register(PacketContentExit.class, ServerHandlerContentExit.class, Side.SERVER);

            register(PacketServerSettings.class, ClientHandlerServerSettings.class, Side.CLIENT);
            register(PacketServerSettings.class, ServerHandlerServerSettings.class, Side.SERVER);
            register(PacketRequestServerSettings.class, ServerHandlerRequestServerSettings.class, Side.SERVER);
            register(PacketStates.class, ClientHandlerStates.class, Side.CLIENT);
            register(PacketStates.class, ServerHandlerStates.class, Side.SERVER);
            register(PacketRequestStates.class, ServerHandlerRequestStates.class, Side.SERVER);

            /* NPCs */
            register(PacketNpcStateChange.class, ClientHandlerNpcStateChange.class, Side.CLIENT);
            register(PacketNpcState.class, ClientHandlerNpcState.class, Side.CLIENT);
            register(PacketNpcState.class, ServerHandlerNpcState.class, Side.SERVER);
            register(PacketNpcList.class, ClientHandlerNpcList.class, Side.CLIENT);
            register(PacketNpcList.class, ServerHandlerNpcList.class, Side.SERVER);
            register(PacketNpcTool.class, ServerHandlerNpcTool.class, Side.SERVER);
            register(PacketNpcJump.class, ServerHandlerNpcJump.class, Side.SERVER);

            /* Quests */
            register(PacketQuest.class, ClientHandlerQuest.class, Side.CLIENT);
            register(PacketQuests.class, ClientHandlerQuests.class, Side.CLIENT);
            register(PacketQuestAction.class, ServerHandlerQuestAction.class, Side.SERVER);
            register(PacketQuestVisibility.class, ServerHandlerQuestVisibility.class, Side.SERVER);

            /* Factions */
            register(PacketRequestFactions.class, ServerHandlerRequestFactions.class, Side.SERVER);

            /* Events */
            register(PacketSyncHotkeys.class, ClientHandlerSyncHotkeys.class, Side.CLIENT);
            register(PacketTriggeredHotkeys.class, ServerHandlerHotkeys.class, Side.SERVER);

            /* Scripts */
            register(PacketEntityRotations.class, ClientHandlerEntityRotations.class, Side.CLIENT);
            register(PacketClick.class, ServerHandlerClick.class, Side.SERVER);
            register(PacketClipboard.class, ClientHandlerClipboard.class, Side.CLIENT);
            register(PacketRepl.class, ClientHandlerRepl.class, Side.CLIENT);
            register(PacketRepl.class, ServerHandlerRepl.class, Side.SERVER);
            register(PacketSound.class, ClientHandlerSound.class, Side.CLIENT);
            register(PacketWorldMorph.class, ClientHandlerWorldMorph.class, Side.CLIENT);
            register(PacketPlayAnimation.class, PacketPlayAnimation.ClientHandler.class, Side.CLIENT);
            register(PacketOpenLink.class, ClientHandlerOpenLink.class, Side.CLIENT);


            register(PacketClientSettings.class, ClientSettingsHandler.class, Side.CLIENT);
            register(PacketClientSettings.class, ServerClientSettingsHandler.class, Side.SERVER);

            /* HUD & UI */
            register(PacketHUDScene.class, ClientHandlerHUDScene.class, Side.CLIENT);
            register(PacketHUDMorph.class, ClientHandlerHUDMorph.class, Side.CLIENT);

            register(PacketUI.class, ClientHandlerUI.class, Side.CLIENT);
            register(PacketUI.class, ServerHandlerUI.class, Side.SERVER);
            register(PacketUIData.class, ClientHandlerUIData.class, Side.CLIENT);
            register(PacketUIData.class, ServerHandlerUIData.class, Side.SERVER);
            register(PacketCloseUI.class, ClientHandlerCloseUI.class, Side.CLIENT);

            /* Logs */
            register(PacketRequestLogs.class, ServerHandlerLogs.class, Side.SERVER);
            register(PacketLogs.class, ClientHandlerLogs.class, Side.CLIENT);

            /* Utils */
            register(PacketChangedBoundingBox.class, ClientHandlerChangedBoundingBox.class, Side.CLIENT);

            register(PacketPack.class, ClientHandlerPack.class, Side.CLIENT);
            register(PacketBlackAndWhiteShader.class, ClientHandlerBlackAndWhiteShader.class, Side.CLIENT);
            register(PacketLockPerspective.class, ClientHandlerLockPerspective.class, Side.CLIENT);
        }
    };

    /**
     * Send message to players who are tracking given entity
     */
    public static void sendToTracked(Entity entity, IMessage message) {
        EntityTracker tracker = ((WorldServer) entity.world).getEntityTracker();

        for (EntityPlayer player : tracker.getTrackingPlayers(entity)) {
            sendTo(message, (EntityPlayerMP) player);
        }
    }

    /**
     * Send message to given player
     */
    public static void sendTo(IMessage message, EntityPlayerMP player) {
        DISPATCHER.sendTo(message, player);
    }

    /**
     * Send message to the server
     */
    public static void sendToServer(IMessage message) {
        DISPATCHER.sendToServer(message);
    }

    /**
     * Register all the networking messages and message handlers
     */
    public static void register() {
        DISPATCHER.register();
    }
}