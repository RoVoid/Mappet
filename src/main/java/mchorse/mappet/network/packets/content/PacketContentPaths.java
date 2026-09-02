package mchorse.mappet.network.packets.content;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.utils.content.IContentTypeBase;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.HashSet;
import java.util.Set;

public class PacketContentPaths extends PacketContentBase {
    public Set<String> paths = new HashSet<>();

    public String renameOld;
    public String renameNew;

    public PacketContentPaths() {
        super();
    }

    public PacketContentPaths(IContentTypeBase type, Set<String> paths) {
        super(type);
        this.paths.addAll(paths);
    }

    public PacketContentPaths(IContentTypeBase type, Set<String> paths, int requestId) {
        super(type, requestId);
        this.paths.addAll(paths);
    }

    public PacketContentPaths rename(String oldPath, String newPath) {
        renameOld = oldPath;
        renameNew = newPath;
        return this;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        for (int i = 0, c = buf.readInt(); i < c; i++) paths.add(ByteBufUtils.readUTF8String(buf));
        if (buf.readBoolean()) {
            renameOld = ByteBufUtils.readUTF8String(buf);
            renameNew = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(paths.size());
        for (String path : paths) ByteBufUtils.writeUTF8String(buf, path);
        buf.writeBoolean(renameOld != null && renameNew != null);
        if (renameOld != null && renameNew != null) {
            ByteBufUtils.writeUTF8String(buf, renameOld);
            ByteBufUtils.writeUTF8String(buf, renameNew);
        }
    }
}