package mchorse.mappet.network.client;

import mchorse.mappet.Mappet;
import mchorse.mappet.network.common.PacketBlackAndWhiteShader;
import mchorse.mappet.network.common.PacketPack;
import mchorse.mclib.network.ClientMessageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientHandlerBlackAndWhiteShader extends ClientMessageHandler<PacketBlackAndWhiteShader> {
    @Override
    public void run(EntityPlayerSP entityPlayerSP, PacketBlackAndWhiteShader message) {
        Minecraft.getMinecraft().addScheduledTask(() -> setBlackAndWhiteShader(message.isEnable()));
    }

    public static void setBlackAndWhiteShader(boolean enable) {
        if(!OpenGlHelper.shadersSupported) return;
        if (!Minecraft.getMinecraft().entityRenderer.isShaderActive() && enable) {
            Minecraft.getMinecraft().entityRenderer.loadShader(new ResourceLocation(Mappet.MOD_ID, "shaders/post/blackandwhite.json"));
        }
        else if (Minecraft.getMinecraft().entityRenderer.isShaderActive() && !enable) {
            Minecraft.getMinecraft().entityRenderer.stopUseShader();
        }
    }
}
