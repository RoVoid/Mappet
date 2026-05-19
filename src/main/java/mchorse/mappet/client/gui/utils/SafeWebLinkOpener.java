package mchorse.mappet.client.gui.utils;

import com.google.common.collect.Sets;
import mchorse.mappet.Mappet;
import mchorse.mappet.config.MappetConfig;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.scripts.PacketOpenLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

public class SafeWebLinkOpener implements GuiYesNoCallback {
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");

    private String pendingUrl = "";

    @Nullable
    public static URI parseUrl(String url) {
        if (url == null || url.isEmpty()) {
            Mappet.logger.error("Empty link: " + url);
            return null;
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Mappet.logger.error("Invalid link " + url);
            return null;
        }

        String s = uri.getScheme();
        if (s == null) {
            Mappet.logger.error("Missing protocol in " + url);
            return null;
        }
        if (!PROTOCOLS.contains(s.toLowerCase(Locale.ROOT))) {
            Mappet.logger.error("Unsupported protocol in " + url);
            return null;
        }

        return uri;
    }

    public static void requestToOpenWebLink(String url, EntityPlayerMP player) {
        if (parseUrl(url) == null) return;
        Dispatcher.sendTo(new PacketOpenLink(url), player);
    }

    @SideOnly(Side.CLIENT)
    public void requestToOpenWebLink(String url) {
        URI uri;
        if ((uri = parseUrl(url)) == null) return;

        if (MappetConfig.immediatelyOpenLink.get()) {
            pendingUrl = "";
            openWebLink(uri);
        }
        else {
            pendingUrl = url;
            Minecraft.getMinecraft().displayGuiScreen(new GuiConfirmOpenLink(this, url, 0, false));
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (pendingUrl.isEmpty()) return;

        String url = pendingUrl;
        pendingUrl = "";

        Minecraft.getMinecraft().player.closeScreen();

        if (!result) return;

        URI uri = parseUrl(url);
        if (uri != null) openWebLink(uri);
    }


    private void openWebLink(URI url) {
        try {
            Desktop.getDesktop().browse(url);
        } catch (Exception e) {
            Mappet.logger.error("Couldn't open link: {}", e.getMessage());
        }
    }
}
