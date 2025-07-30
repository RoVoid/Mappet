package mchorse.mappet.network.client.events;

import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.client.KeyboardHandler;
import mchorse.mappet.network.common.events.PacketSyncHotkeys;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class ClientHandlerSyncHotkeys extends ClientMessageHandler<PacketSyncHotkeys> {
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketSyncHotkeys message) {
        Map<String, Integer> clientKeys = new HashMap<>();

        if (KeyboardHandler.hotkeysNeedLoad) KeyboardHandler.loadClientKeys(clientKeys, message.hotkeys);
        else for (Hotkey hotkey : KeyboardHandler.hotkeys.values())
            if (hotkey.keycode != -1) clientKeys.put(hotkey.name, hotkey.keycode);

        KeyboardHandler.hotkeys.clear();

        for (Hotkey hotkey : message.hotkeys) {
            hotkey.keycode = clientKeys.getOrDefault(hotkey.name, -1);
            KeyboardHandler.hotkeys.put(hotkey.name, hotkey);
        }

        KeyboardHandler.clientPlayerJournal = !message.journalTrigger;
    }
}
