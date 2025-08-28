package mchorse.mappet.network.common;

import io.netty.buffer.ByteBuf;
import mchorse.mappet.api.scripts.code.math.ScriptVector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class PacketCamera implements IMessage {
    private UUID uuid;

    public PacketCamera() {
    }

    public PacketCamera(Entity entity) {
        uuid = entity.getUniqueID();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        uuid = UUID.fromString(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, uuid.toString());
    }

    public UUID getUuid() {
        return uuid;
    }
}
