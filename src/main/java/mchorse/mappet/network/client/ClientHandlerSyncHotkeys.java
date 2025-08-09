package mchorse.mappet.network.client;

import mchorse.mappet.api.hotkeys.Hotkey;
import mchorse.mappet.client.KeyboardHandler;
import mchorse.mappet.network.common.hotkey.PacketSyncHotkeys;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientHandlerSyncHotkeys extends ClientMessageHandler<PacketSyncHotkeys> {
    @Override
    @SideOnly(Side.CLIENT)
    public void run(EntityPlayerSP player, PacketSyncHotkeys message) {
        KeyboardHandler.loadClientKeys(message.hotkeys);

        KeyboardHandler.hotkeys.clear();
        for (Hotkey hotkey : message.hotkeys) {
            KeyboardHandler.hotkeys.put(hotkey.id, hotkey);
        }
    }
}
