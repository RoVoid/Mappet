package mchorse.mappet.network.packets.content;

import mchorse.mappet.api.utils.content.IContentTypeBase;

public class PacketContentRequestPaths extends PacketContentBase
{
    public PacketContentRequestPaths()
    {
        super();
    }

    public PacketContentRequestPaths(IContentTypeBase type)
    {
        super(type);
    }

    public PacketContentRequestPaths(IContentTypeBase type, int requestId)
    {
        super(type, requestId);
    }
}