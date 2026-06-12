package mchorse.mappet.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerUtils {
    public static boolean isOperator(EntityPlayer player) {
        MinecraftServer server = player.getServer();
        return server != null && (!server.isDedicatedServer() || server.getPlayerList().canSendCommands(player.getGameProfile()));
    }

    @SideOnly(Side.CLIENT)
    public static boolean isOperator() {
        return Minecraft.getMinecraft().player.canUseCommand(2, "");
    }

    @SideOnly(Side.CLIENT)
    public static String getLanguage() {
        return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
    }
}
