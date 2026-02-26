package mchorse.mappet.api.translations;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import mchorse.mappet.Mappet;
import mchorse.mappet.api.utils.manager.BaseManager;
import net.minecraft.nbt.NBTTagCompound;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TranslationManager extends BaseManager<Translation> {
    public static final Set<String> localesCodes = new ObjectArraySet<>(Arrays.asList("af_za", // Afrikaans (South Africa)
            "ar_sa", // Arabic (Saudi Arabia)
            "ast_es", // Asturian (Spain)
            "az_az", // Azerbaijani (Azerbaijan)
            "be_by", // Belarusian (Belarus)
            "bg_bg", // Bulgarian (Bulgaria)
            "br_fr", // Breton (France)
            "bs_ba", // Bosnian (Bosnia and Herzegovina)
            "ca_es", // Catalan (Spain)
            "cs_cz", // Czech (Czech Republic)
            "cy_gb", // Welsh (United Kingdom)
            "da_dk", // Danish (Denmark)
            "de_at", // German (Austria)
            "de_ch", // German (Switzerland)
            "de_de", // German (Germany)
            "el_gr", // Greek (Greece)
            "en_au", // English (Australia)
            "en_ca", // English (Canada)
            "en_gb", // English (United Kingdom)
            "en_nz", // English (New Zealand)
            "en_pt", // English (Pirate Speak)
            "en_ud", // English (Upside Down)
            "en_us", // English (United States)
            "eo_uy", // Esperanto (Uruguay)
            "es_ar", // Spanish (Argentina)
            "es_cl", // Spanish (Chile)
            "es_es", // Spanish (Spain)
            "es_mx", // Spanish (Mexico)
            "es_uy", // Spanish (Uruguay)
            "es_ve", // Spanish (Venezuela)
            "et_ee", // Estonian (Estonia)
            "eu_es", // Basque (Spain)
            "fa_ir", // Persian (Iran)
            "fi_fi", // Finnish (Finland)
            "fo_fo", // Faroese (Faroe Islands)
            "fr_ca", // French (Canada)
            "fr_fr", // French (France)
            "fy_nl", // Frisian (Netherlands)
            "ga_ie", // Irish (Ireland)
            "gd_gb", // Scottish Gaelic (United Kingdom)
            "gl_es", // Galician (Spain)
            "gv_im", // Manx (Isle of Man)
            "haw_us", // Hawaiian (United States)
            "he_il", // Hebrew (Israel)
            "hi_in", // Hindi (India)
            "hr_hr", // Croatian (Croatia)
            "hu_hu", // Hungarian (Hungary)
            "hy_am", // Armenian (Armenia)
            "id_id", // Indonesian (Indonesia)
            "ig_ng", // Igbo (Nigeria)
            "io_en", // Ido (International)
            "is_is", // Icelandic (Iceland)
            "it_it", // Italian (Italy)
            "ja_jp", // Japanese (Japan)
            "jbo_en", // Lojban (Logical Language)
            "ka_ge", // Georgian (Georgia)
            "kn_in", // Kannada (India)
            "ko_kr", // Korean (South Korea)
            "kw_gb", // Cornish (United Kingdom)
            "la_la", // Latin (Vatican State)
            "lb_lu", // Luxembourgish (Luxembourg)
            "li_li", // Limburgish (Limburg)
            "lol_us", // LOLCAT (United States)
            "lt_lt", // Lithuanian (Lithuania)
            "lv_lv", // Latvian (Latvia)
            "mi_nz", // Maori (New Zealand)
            "mk_mk", // Macedonian (North Macedonia)
            "mn_mn", // Mongolian (Mongolia)
            "ms_my", // Malay (Malaysia)
            "mt_mt", // Maltese (Malta)
            "nds_de", // Low German (Germany)
            "nl_be", // Dutch (Belgium)
            "nl_nl", // Dutch (Netherlands)
            "nn_no", // Norwegian Nynorsk (Norway)
            "no_no", // Norwegian Bokmål (Norway)
            "oc_fr", // Occitan (France)
            "pl_pl", // Polish (Poland)
            "pt_br", // Portuguese (Brazil)
            "pt_pt", // Portuguese (Portugal)
            "qya_aa", // Quenya (Arda)
            "ro_ro", // Romanian (Romania)
            "ru_ru", // Russian (Russia)
            "se_no", // Northern Sami (Norway)
            "sk_sk", // Slovak (Slovakia)
            "sl_si", // Slovenian (Slovenia)
            "so_so", // Somali (Somalia)
            "sq_al", // Albanian (Albania)
            "sr_sp", // Serbian (Serbia)
            "sv_se", // Swedish (Sweden)
            "ta_in", // Tamil (India)
            "th_th", // Thai (Thailand)
            "tlh_aa", // Klingon (Star Trek)
            "tr_tr", // Turkish (Turkey)
            "uk_ua", // Ukrainian (Ukraine)
            "val_es", // Valencian (Spain)
            "vi_vn", // Vietnamese (Vietnam)
            "zh_cn", // Chinese (China, Simplified)
            "zh_tw"  // Chinese (Taiwan, Traditional)
    ));

    public TranslationManager(File folder) {
        super(folder);
    }

    public String getString(String locale, String key, Object... args) {
        if (locale == null || key == null) return "";
        Translation translation = load(key);

        if (translation == null) {
            Mappet.logger.warning("Missing translation file: " + key + getExtension());
            return key;
        }

        String str = translation.entries.get(locale);

        if (str == null) {
            Mappet.logger.warning("Missing translation locale: " + locale + " in " + key);
            return key;
        }

        str = str.replace("\\\\", "\\");
        try {
            return MessageFormat.format(str, args);
        } catch (IllegalArgumentException e) {
            Mappet.logger.warning("Error formatting translation key: " + key + " with " + locale);
            return str;
        }
    }

    @Override
    protected Translation createData(String key, NBTTagCompound tag) {
        Translation translation = new Translation();
        translation.deserializeNBT(tag);
        return translation;
    }

    @Override
    public Collection<String> getKeys() {
        Set<String> set = new HashSet<>();
        if (folder == null) return set;

        File[] files = folder.listFiles();
        if (files == null) return set;
        for (File file : files) {
            String name = file.getName();
            if (file.isFile() && name.endsWith(getExtension())) set.add(name.replace(getExtension(), ""));
        }

        return set;
    }

    @Override
    public boolean save(String key, NBTTagCompound tag) {
        if (isValid(tag)) return super.save(key, tag);
        return false;
    }

    private boolean isValid(NBTTagCompound tag) {
        if (tag == null) return false;
        NBTTagCompound entries = tag.getCompoundTag("Entries");
        if (entries.hasNoTags()) return false;
        for (String locale : entries.getKeySet()) if (!localesCodes.contains(locale)) return false;
        return true;
    }
}