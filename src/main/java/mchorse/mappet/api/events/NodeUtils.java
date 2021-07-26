package mchorse.mappet.api.events;

import mchorse.mappet.api.utils.nodes.Node;
import mchorse.mappet.api.utils.nodes.NodeSystem;
import net.minecraft.nbt.NBTTagCompound;

public class NodeUtils
{
    public static <T extends Node> T nodeFromNBT(NodeSystem<T> system, NBTTagCompound tag)
    {
        String type = tag.getString("Type");
        T node = system.getFactory().create(type);

        node.deserializeNBT(tag);

        return node;
    }

    public static <T extends Node> NBTTagCompound nodeToNBT(T node)
    {
        return node.serializeNBT();
    }
}