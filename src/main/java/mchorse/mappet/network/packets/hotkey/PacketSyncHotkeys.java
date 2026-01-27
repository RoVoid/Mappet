package mchorse.mappet.network.packets.hotkey;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.ServerSettings;
import mchorse.mappet.api.hotkeys.Hotkey;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncHotkeys implements IMessage {
    public List<Hotkey> hotkeys = new ArrayList<>();

    public PacketSyncHotkeys() {
    }

    public PacketSyncHotkeys(ServerSettings settings) {
        hotkeys.addAll(settings.hotkeys.keys.values());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        for (int i = 0, c = buf.readInt(); i < c; i++) {
            hotkeys.add(new Hotkey(ByteBufUtils.readUTF8String(buf), ByteBufUtils.readUTF8String(buf), buf.readInt(), buf.readInt()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(hotkeys.size());
        for (Hotkey hotkey : hotkeys) {
            ByteBufUtils.writeUTF8String(buf, hotkey.id);
            ByteBufUtils.writeUTF8String(buf, hotkey.name);
            buf.writeInt(hotkey.defaultKeycode);
            buf.writeInt(hotkey.mode.ordinal());
        }
    }
}