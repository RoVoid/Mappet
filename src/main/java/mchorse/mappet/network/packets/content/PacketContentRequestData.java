package mchorse.mappet.network.packets.content;

import mchorse.mappet.api.utils.content.IContentTypeBase;

public class PacketContentRequestData extends PacketContentBase {
    public PacketContentRequestData() {
        super();
    }

    public PacketContentRequestData(IContentTypeBase type, String path) {
        super(type, path);
    }
}