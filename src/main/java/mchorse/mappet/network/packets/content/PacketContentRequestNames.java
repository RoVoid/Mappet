package mchorse.mappet.network.packets.content;

import mchorse.mappet.api.utils.content.IContentTypeBase;

public class PacketContentRequestNames extends PacketContentBase
{
    public PacketContentRequestNames()
    {
        super();
    }

    public PacketContentRequestNames(IContentTypeBase type)
    {
        super(type);
    }

    public PacketContentRequestNames(IContentTypeBase type, int requestId)
    {
        super(type, requestId);
    }
}