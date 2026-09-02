package mchorse.mappet.client;

import mchorse.mappet.Mappet;
import mchorse.mappet.api.translations.Translation;
import mchorse.mappet.api.translations.TranslationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Locale;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// AI generated, will fix later
@SideOnly(Side.CLIENT)
public class TranslationApplier
{
    private static TranslationApplier instance;

    /** translationKey → original value (empty string = key didn't exist before) */
    private final Map<String, String> originalValues = new HashMap<>();

    /** translationKey → override value (what we applied) */
    private final Map<String, String> appliedValues = new HashMap<>();

    private Field localePropertiesField;
    private boolean fieldResolved = false;

    /* Singleton */

    public static TranslationApplier get()
    {
        if (instance == null)
        {
            instance = new TranslationApplier();
            MinecraftForge.EVENT_BUS.register(instance);
        }

        return instance;
    }

    /* Public API */

    /**
     * Apply all translations from a Translation object for the current client locale.
     * Falls back to en_us if current locale has no entry.
     */
    public void apply(String translationKey, Translation translation)
    {
        if (translationKey == null || translation == null) return;

        String locale = getCurrentLocale();
        String value = translation.entries.get(locale);

        if (value == null) value = translation.entries.get("en_us");
        if (value == null) return;

        applyKey(translationKey, value);
    }

    /**
     * Apply all translations from manager for current locale.
     * Called on connect/locale change to reapply everything.
     */
    public void applyAll(TranslationManager manager)
    {
        if (manager == null) return;

        Set<String> ids = manager.getPaths();

        for (String id : ids)
        {
            Translation translation = manager.load(id);
            if (translation != null) apply(id, translation);
        }
    }

    /**
     * Rollback a single translation key to its original value.
     */
    public void rollback(String translationKey)
    {
        if (!appliedValues.containsKey(translationKey)) return;

        Map<String, String> map = getLocaleMap();
        if (map == null) return;

        String original = originalValues.remove(translationKey);
        appliedValues.remove(translationKey);

        if (original == null || original.isEmpty()) map.remove(translationKey);
        else map.put(translationKey, original);
    }

    /**
     * Rollback all applied translations to their original values.
     */
    public void rollbackAll()
    {
        Map<String, String> map = getLocaleMap();

        if (map != null)
        {
            for (Map.Entry<String, String> entry : originalValues.entrySet())
            {
                String key = entry.getKey();
                String original = entry.getValue();

                if (original == null || original.isEmpty()) map.remove(key);
                else map.put(key, original);
            }
        }

        originalValues.clear();
        appliedValues.clear();
    }

    /**
     * Returns currently applied overrides (read-only).
     */
    public Map<String, String> getApplied()
    {
        return Collections.unmodifiableMap(appliedValues);
    }

    /* Events */

    /**
     * On disconnect — rollback all overrides, the server is gone.
     */
    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        rollbackAll();
    }

    /**
     * On language change — Minecraft reloads all locale strings,
     * wiping our overrides. Reapply from appliedValues snapshot.
     *
     * We hook ClientChatReceivedEvent as a proxy tick since there's no
     * direct "language reloaded" event in 1.12.2. A cleaner approach is
     * to call reapply() manually from your resource reload hook if you have one.
     */
    public void reapply()
    {
        if (appliedValues.isEmpty()) return;

        // originalValues is now stale — Minecraft reloaded fresh strings.
        // Clear it so we re-snapshot the new originals on next apply.
        originalValues.clear();

        Map<String, String> snapshot = new HashMap<>(appliedValues);
        appliedValues.clear();

        Map<String, String> map = getLocaleMap();
        if (map == null) return;

        for (Map.Entry<String, String> entry : snapshot.entrySet())
        {
            applyKey(entry.getKey(), entry.getValue());
        }
    }

    /* Private */

    private void applyKey(String translationKey, String value)
    {
        Map<String, String> map = getLocaleMap();
        if (map == null) return;

        // save original only on first apply
        if (!originalValues.containsKey(translationKey))
        {
            originalValues.put(translationKey, map.getOrDefault(translationKey, ""));
        }

        map.put(translationKey, value);
        appliedValues.put(translationKey, value);
    }

    private Map<String, String> getLocaleMap()
    {
        Locale locale = getLocaleInstance();
        if (locale == null) return null;

        Field field = getField();
        if (field == null) return null;

        try
        {
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) field.get(locale);
            return map;
        }
        catch (Exception e)
        {
            Mappet.logger.error("TranslationApplier: failed to get locale map: " + e.getMessage());
            return null;
        }
    }

    private Locale getLocaleInstance()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return null;
        return null;
        //return mc.gameSettings == null ? null : (Locale) mc.getLanguageManager().getCurrentLanguage().;
        // Note: if the above cast fails at runtime because getCurrentLanguage()
        // returns Language (not Locale), use reflection to get the Locale field
        // from the language manager instead:
        // ReflectionHelper.getPrivateValue(LanguageManager.class, mc.getLanguageManager(), "currentLocale", "field_...");
    }

    private Field getField()
    {
        if (fieldResolved) return localePropertiesField;

        fieldResolved = true;

        try
        {
//            FieldUtils.readField()
            // dev name: "properties", obf name: "field_135032_a"
            localePropertiesField = ReflectionHelper.findField(Locale.class, "properties", "field_135032_a");
            localePropertiesField.setAccessible(true);
        }
        catch (Exception e)
        {
            Mappet.logger.error("TranslationApplier: failed to find Locale.properties field: " + e.getMessage());
            localePropertiesField = null;
        }

        return localePropertiesField;
    }

    private String getCurrentLocale()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getLanguageManager() == null) return "en_us";
        return mc.getLanguageManager().getCurrentLanguage().getLanguageCode();
    }
}