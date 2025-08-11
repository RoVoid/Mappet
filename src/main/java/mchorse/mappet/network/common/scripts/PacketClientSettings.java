package mchorse.mappet.network.common.scripts;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class PacketClientSettings implements IMessage {
    public List<String> requests = new ArrayList<>();
    public NBTTagCompound options = new NBTTagCompound();

    public String script = "";
    public String function = "";

    public PacketClientSettings() {
    }

    public PacketClientSettings(List<String> requests, NBTTagCompound options, String script, String function) {
        this.requests = requests != null ? new ArrayList<>(requests) : new ArrayList<>();
        this.options = options != null ? options : new NBTTagCompound();
        this.script = script != null ? script : "";
        this.function = function != null ? function : "";
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        requests = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            requests.add(ByteBufUtils.readUTF8String(buf));
        }

        options = ByteBufUtils.readTag(buf);
        script = ByteBufUtils.readUTF8String(buf);
        function = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(requests.size());
        for (String req : requests) ByteBufUtils.writeUTF8String(buf, req);
        ByteBufUtils.writeTag(buf, options);
        ByteBufUtils.writeUTF8String(buf, script);
        ByteBufUtils.writeUTF8String(buf, function);
    }

    @Override
    public String toString() {
        return "PacketClientSettings{" +
                "script='" + script + '\'' +
                ", function='" + function + '\'' +
                ", requests=" + requests +
                ", options=" + options +
                '}';
    }
}
