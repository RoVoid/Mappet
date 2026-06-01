package mchorse.mappet.addon;

import mchorse.mappet.Mappet;

import java.util.HashMap;
import java.util.Map;

public class AddonManager {
    static Map<String, IMappetAddon> addons = new HashMap<>();

    public static void add(IMappetAddon addon) {
        if (addon == null) {
            Mappet.logger.warn("Addon cannot be null");
            return;
        }
        if ("mappet".equals(addon.id())) {
            Mappet.logger.warn("Addon " + addon.str() + " was not added, because it's id is 'mappet'");
            return;
        }
        IMappetAddon existing = addons.putIfAbsent(addon.id(), addon);
        if (existing != null) Mappet.logger.warn("Addon " + addon.str() + " was not added, because exists " + existing.str());
    }

    public static void list() {
        StringBuilder sb = new StringBuilder("Mappet addons:\n - Mappet (mappet:"+"%VERSION% by RoVoid)\n");
        for (IMappetAddon addon : addons.values()) sb.append(" - ").append(addon.str()).append('\n');
        Mappet.logger.info(sb.toString());
    }
}
