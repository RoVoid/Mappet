package mchorse.mappet.network;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.client.*;
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
import mchorse.mappet.network.packets.*;
import mchorse.mappet.network.packets.blocks.PacketEditConditionModel;
import mchorse.mappet.network.packets.blocks.PacketEditEmitter;
import mchorse.mappet.network.packets.blocks.PacketEditRegion;
import mchorse.mappet.network.packets.blocks.PacketEditTrigger;
import mchorse.mappet.network.packets.content.*;
import mchorse.mappet.network.packets.dialogue.PacketDialogueFragment;
import mchorse.mappet.network.packets.dialogue.PacketFinishDialogue;
import mchorse.mappet.network.packets.dialogue.PacketPickReply;
import mchorse.mappet.network.packets.factions.PacketRequestFactions;
import mchorse.mappet.network.packets.hotkey.PacketSyncHotkeys;
import mchorse.mappet.network.packets.hotkey.PacketTriggeredHotkeys;
import mchorse.mappet.network.packets.huds.PacketHUDMorph;
import mchorse.mappet.network.packets.huds.PacketHUDScene;
import mchorse.mappet.network.packets.items.PacketScriptedItemInfo;
import mchorse.mappet.network.packets.logs.PacketLogs;
import mchorse.mappet.network.packets.logs.PacketRequestLogs;
import mchorse.mappet.network.packets.npc.*;
import mchorse.mappet.network.packets.quests.PacketQuest;
import mchorse.mappet.network.packets.quests.PacketQuestAction;
import mchorse.mappet.network.packets.quests.PacketQuestVisibility;
import mchorse.mappet.network.packets.quests.PacketQuests;
import mchorse.mappet.network.packets.scripts.*;
import mchorse.mappet.network.packets.ui.PacketCloseUI;
import mchorse.mappet.network.packets.ui.PacketUI;
import mchorse.mappet.network.packets.ui.PacketUIData;
import mchorse.mappet.network.packets.utils.PacketChangedBoundingBox;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Network dispatcher
 */
public class Dispatcher {
    private static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(Mappet.MOD_ID);
    private static byte size = 0;

    public static <REQ extends IMessage, REPLY extends IMessage> void push(Class<REQ> message, Class<? extends IMessageHandler<REQ, REPLY>> handler, Side side) {
        dispatcher.registerMessage(handler, message, size++, side);
    }

    public static <REQ extends IMessage, REPLY extends IMessage> void push(Class<REQ> message, Class<? extends IMessageHandler<REQ, REPLY>> clientHandler, Class<? extends IMessageHandler<REQ, REPLY>> serverHandler) {
        dispatcher.registerMessage(clientHandler, message, size++, Side.CLIENT);
        dispatcher.registerMessage(serverHandler, message, size++, Side.SERVER);
    }

    /**
     * Send message to players who are tracking given entity
     */
    public static void sendToTracked(Entity entity, IMessage message) {
        EntityTracker tracker = ((WorldServer) entity.world).getEntityTracker();
        for (EntityPlayer player : tracker.getTrackingPlayers(entity)) sendTo(message, (EntityPlayerMP) player);
    }

    /**
     * Send message to given player
     */
    public static void sendTo(IMessage message, EntityPlayerMP player) {
        dispatcher.sendTo(message, player);
    }

    /**
     * Send message to the server
     */
    public static void sendToServer(IMessage message) {
        dispatcher.sendToServer(message);
    }

    /**
     * Send message to all players
     */
    public static void sendToAll(IMessage message) {
        dispatcher.sendToAll(message);
    }

    public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        dispatcher.sendToAllAround(message, point);
    }

    /**
     * Register all the networking messages and message handlers
     */
    public static void register() {
        /* Dialogue */
        push(PacketDialogueFragment.class, ClientHandlerDialogueFragment.class, Side.CLIENT);
        push(PacketPickReply.class, ServerHandlerPickReply.class, Side.SERVER);
        push(PacketFinishDialogue.class, ServerHandlerFinishDialogue.class, Side.SERVER);

        /* Blocks */
        push(PacketEditEmitter.class, ClientHandlerEditEmitter.class, ServerHandlerEditEmitter.class);
        push(PacketEditTrigger.class, ClientHandlerEditTrigger.class, ServerHandlerEditTrigger.class);
        push(PacketEditRegion.class, ClientHandlerEditRegion.class, ServerHandlerEditRegion.class);
        push(PacketEditConditionModel.class, ClientHandlerEditConditionModel.class, ServerHandlerEditConditionModel.class);

        /* Scripted item */
        push(PacketScriptedItemInfo.class, ClientHandlerScriptedItemInfo.class, ServerHandlerScriptedItemInfo.class);

        /* Creative editing */
        push(PacketContentRequestNames.class, ServerHandlerContentRequestNames.class, Side.SERVER);
        push(PacketContentRequestData.class, ServerHandlerContentRequestData.class, Side.SERVER);
        push(PacketContentData.class, ClientHandlerContentData.class, ServerHandlerContentData.class);
        push(PacketContentFolder.class, ServerHandlerContentFolder.class, Side.SERVER);
        push(PacketContentNames.class, ClientHandlerContentNames.class, Side.CLIENT);
        push(PacketContentExit.class, ServerHandlerContentExit.class, Side.SERVER);

        push(PacketServerSettings.class, ClientHandlerServerSettings.class, ServerHandlerServerSettings.class);
        push(PacketRequestServerSettings.class, ServerHandlerRequestServerSettings.class, Side.SERVER);
        push(PacketStates.class, ClientHandlerStates.class, ServerHandlerStates.class);
        push(PacketRequestStates.class, ServerHandlerRequestStates.class, Side.SERVER);

        /* NPCs */
        push(PacketNpcStateChange.class, ClientHandlerNpcStateChange.class, Side.CLIENT);
        push(PacketNpcState.class, ClientHandlerNpcState.class, ServerHandlerNpcState.class);
        push(PacketNpcList.class, ClientHandlerNpcList.class, ServerHandlerNpcList.class);
        push(PacketNpcTool.class, ServerHandlerNpcTool.class, Side.SERVER);
        push(PacketNpcJump.class, ServerHandlerNpcJump.class, Side.SERVER);

        /* Quests */
        push(PacketQuest.class, ClientHandlerQuest.class, Side.CLIENT);
        push(PacketQuests.class, ClientHandlerQuests.class, Side.CLIENT);
        push(PacketQuestAction.class, ServerHandlerQuestAction.class, Side.SERVER);
        push(PacketQuestVisibility.class, ServerHandlerQuestVisibility.class, Side.SERVER);

        /* Factions */
        push(PacketRequestFactions.class, ServerHandlerRequestFactions.class, Side.SERVER);

        /* Events */
        push(PacketSyncHotkeys.class, ClientHandlerSyncHotkeys.class, Side.CLIENT);
        push(PacketTriggeredHotkeys.class, ServerHandlerHotkeys.class, Side.SERVER);
        push(PacketCamera.class, ClientHandlerCamera.class, Side.CLIENT);
        push(PacketScreenshot.class, ClientHandlerScreenshot.class, Side.CLIENT);
        //  push(PacketScreenshot.class, ServerHandlerScre.class, Side.CLIENT);

        /* Scripts */
        push(PacketEntityRotations.class, ClientHandlerEntityRotations.class, Side.CLIENT);
        push(PacketClick.class, ServerHandlerClick.class, Side.SERVER);
        push(PacketClipboard.class, ClientHandlerClipboard.class, Side.CLIENT);
        push(PacketRepl.class, ClientHandlerRepl.class, ServerHandlerRepl.class);
        push(PacketSound.class, ClientHandlerSound.class, Side.CLIENT);
        push(PacketWorldMorph.class, ClientHandlerWorldMorph.class, Side.CLIENT);
        push(PacketPlayAnimation.class, PacketPlayAnimation.ClientHandler.class, Side.CLIENT);
        push(PacketOpenLink.class, ClientHandlerOpenLink.class, Side.CLIENT);

        push(PacketClientSettings.class, ClientSettingsHandler.class, ServerClientSettingsHandler.class);

        /* HUD & UI */
        push(PacketHUDScene.class, ClientHandlerHUDScene.class, Side.CLIENT);
        push(PacketHUDMorph.class, ClientHandlerHUDMorph.class, Side.CLIENT);

        push(PacketUI.class, ClientHandlerUI.class, ServerHandlerUI.class);
        push(PacketUIData.class, ClientHandlerUIData.class, ServerHandlerUIData.class);
        push(PacketCloseUI.class, ClientHandlerCloseUI.class, Side.CLIENT);

        /* Logs */
        push(PacketRequestLogs.class, ServerHandlerLogs.class, Side.SERVER);
        push(PacketLogs.class, ClientHandlerLogs.class, Side.CLIENT);

        /* Utils */
        push(PacketChangedBoundingBox.class, ClientHandlerChangedBoundingBox.class, Side.CLIENT);

        push(PacketPack.class, ClientHandlerPack.class, Side.CLIENT);
        push(PacketBlackAndWhiteShader.class, ClientHandlerBlackAndWhiteShader.class, Side.CLIENT);
        push(PacketPlayerPerspective.class, ClientHandlerPlayerPerspective.class, Side.CLIENT);
    }
}