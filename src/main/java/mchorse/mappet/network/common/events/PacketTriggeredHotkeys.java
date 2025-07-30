package mchorse.mappet.network.common.events;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.hotkeys.HotkeyState;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashSet;
import java.util.Set;

public class PacketTriggeredHotkeys implements IMessage {
    public final Set<HotkeyState> hotkeys;

    public PacketTriggeredHotkeys() {
        hotkeys = new HashSet<>();
    }

    public PacketTriggeredHotkeys(Set<HotkeyState> hotkeys) {
        this.hotkeys = hotkeys;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hotkeys.clear();
        while (buf.isReadable()) {
            hotkeys.add(HotkeyState.of(ByteBufUtils.readUTF8String(buf), buf.readBoolean()));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (HotkeyState hotkey : hotkeys) {
            ByteBufUtils.writeUTF8String(buf, hotkey.name);
            buf.writeBoolean(hotkey.state);
        }
    }
}