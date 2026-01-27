package mchorse.mappet.network.client;

import mchorse.mappet.CommonProxy;
import mchorse.mappet.Mappet;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.PacketScreenshot;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ScreenShotHelper;

import java.io.File;
import java.nio.file.Files;

public class ClientHandlerScreenshot extends ClientMessageHandler<PacketScreenshot> {
    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketScreenshot message) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            File screenshotDir = new File(CommonProxy.configFolder, "screenshots");
            if (!screenshotDir.exists()) screenshotDir.mkdirs();

            if (message.getData() != null && message.getData().length > 0) {
                File file = new File(screenshotDir, message.getName() + ".png");
                Files.write(file.toPath(), message.getData());
                return;
            }

            ScreenShotHelper.saveScreenshot(screenshotDir, message.getName(), mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
            if (message.needShare()) {
                byte[] image = Files.readAllBytes(new File(screenshotDir, message.getName() + ".png").toPath());
                Dispatcher.sendToServer(new PacketScreenshot(message.getName(), image));
            }
        } catch (Exception e) {
            Mappet.loggerClient.error(e.getMessage());
        }
    }
}
