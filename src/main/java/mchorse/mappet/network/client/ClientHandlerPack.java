package mchorse.mappet.network.client;

import mchorse.mappet.network.packets.PacketPack;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientHandlerPack extends ClientMessageHandler<PacketPack> {
    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketPack message) {
        Minecraft.getMinecraft().addScheduledTask(() -> applyResourcePack(message.getPackData()));
    }

    private void applyResourcePack(byte[] packData) {
        try {
            File tempPack = File.createTempFile("resourcepack", ".zip");
            try (FileOutputStream fos = new FileOutputStream(tempPack)) {
                fos.write(packData);
            }
            Minecraft.getMinecraft().getResourcePackRepository().setServerResourcePack(tempPack);
            if (tempPack.exists() && !tempPack.delete()) {
                System.err.println("Не удалось удалить временный файл ресурс-пака: " + tempPack.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
