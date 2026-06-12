package mchorse.mappet.client.gui.url;

import com.google.common.collect.Sets;
import mchorse.mappet.Mappet;
import mchorse.mappet.config.MappetConfig;
import mchorse.mappet.network.Dispatcher;
import mchorse.mappet.network.packets.scripts.PacketOpenLink;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

// Should change to static class?
public class SafeWebLinkOpener {
    private static final Set<String> PROTOCOLS = Sets.newHashSet("http", "https");

    @Nullable
    private URI pendingUrl = null;

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
        if (parseUrl(url) != null) {Dispatcher.sendTo(new PacketOpenLink(url), player);}
    }

    @SideOnly(Side.CLIENT)
    public void requestToOpenWebLink(String url) {
        URI uri = parseUrl(url);
        if (uri == null) return;

        String domain = uri.getHost();
        if (MappetConfig.immediatelyOpenLink.get() || MappetConfig.trustedDomains.get().contains(domain)) {
            openWebLink(uri);
        } else {
            pendingUrl = uri;
            Minecraft.getMinecraft().displayGuiScreen(new GuiLinkOpenScreen(url, this::confirm));
        }
    }

    public void confirm(boolean result, boolean trust) {
        if (pendingUrl == null) return;

        URI uri = pendingUrl;
        pendingUrl = null;

        if (!result) return;

        if (trust) {
            String domain = uri.getHost();
            String current = MappetConfig.trustedDomains.get();
            if (!current.contains(domain)) MappetConfig.trustedDomains.setValue(domain + ' ' + current);
        }

        openWebLink(uri);
    }


    private void openWebLink(URI url) {
        try {
            Desktop.getDesktop().browse(url);
        } catch (Exception e) {
            Mappet.logger.error("Couldn't open link: {}", e.getMessage());
        }
    }

    @Nullable
    public static String getLinkDomain(String link) {
        URI uri = parseUrl(link);
        return uri == null ? null : uri.getHost();
    }
}
