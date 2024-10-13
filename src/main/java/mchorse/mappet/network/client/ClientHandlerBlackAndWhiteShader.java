package mchorse.mappet.network.client;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.common.PacketBlackAndWhiteShader;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

public class ClientHandlerBlackAndWhiteShader extends ClientMessageHandler<PacketBlackAndWhiteShader> {
    static boolean enable = false;
    public static int previousPerspective = Minecraft.getMinecraft().gameSettings.thirdPersonView;

    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketBlackAndWhiteShader message) {
        enableBlackAndWhiteShader(message.isEnable());
    }

    public static void enableBlackAndWhiteShader(boolean newEnable) {
        enable = newEnable;
        update();
    }

    public static void update() {
        if (!OpenGlHelper.shadersSupported) return;
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (!Minecraft.getMinecraft().entityRenderer.isShaderActive() && enable) {
                Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation(Mappet.MOD_ID, "shaders/post/blackandwhite.json"));
            } else if (Minecraft.getMinecraft().entityRenderer.isShaderActive() && !enable) {
                Minecraft.getMinecraft().entityRenderer.stopUseShader();
            }
        });
    }
}
