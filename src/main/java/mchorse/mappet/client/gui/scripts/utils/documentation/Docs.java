package mchorse.mappet.client.gui.scripts.utils.documentation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mchorse.mappet.Mappet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Docs {
    public List<DocEntry> classes = new ArrayList<>();
    public List<DocMethod> methods = new ArrayList<>();
    public String source = "Mappet";

    public Docs() {
        Minecraft mc = Minecraft.getMinecraft();
        Gson gson = new GsonBuilder().create();

        InputStream stream = null;
        String language = mc.getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
        try {
            stream = mc.getResourceManager().getResource(new ResourceLocation(Mappet.MOD_ID, "docs/" + language + ".json")).getInputStream();
        } catch (Exception e) {
            Mappet.loggerClient.error("Not found docs on your localization!");
            if (language.equalsIgnoreCase("en_us")) return;
            try {
                stream = mc.getResourceManager().getResource(new ResourceLocation(Mappet.MOD_ID, "docs/en_us.json")).getInputStream();
            } catch (Exception e1) {
                Mappet.loggerClient.error("Not found docs");
            }
        }

        if (stream == null) return;

        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            String json = scanner.useDelimiter("\\A").next();

            Type type = new TypeToken<Map<String, RawClassEntry>>() {
            }.getType();
            Map<String, RawClassEntry> rawDocs = gson.fromJson(json, type);

            for (Map.Entry<String, RawClassEntry> entry : rawDocs.entrySet()) {
                RawClassEntry raw = entry.getValue();
                DocEntry docEntry = new DocEntry(entry.getKey());
                docEntry.doc = raw.docs == null ? "" : raw.docs;

                if (raw.methods != null) {
                    for (Map.Entry<String, List<RawMethodVariant>> m : raw.methods.entrySet()) {
                        String methodName = m.getKey();
                        List<RawMethodVariant> variants = m.getValue();

                        DocMethod method = new DocMethod(methodName);
                        method.isDeprecated = true;

                        for (RawMethodVariant variantRaw : variants) {
                            DocMethodVariant variant = new DocMethodVariant(methodName);
                            variant.doc = variantRaw.docs == null ? "" : variantRaw.docs;

                            if (variantRaw.returns != null) {
                                DocVariable returns = new DocVariable();
                                returns.type = variantRaw.returns.type;
                                returns.doc = variantRaw.returns.docs == null ? "" : variantRaw.returns.docs;
                                variant.returns = returns;
                            }

                            if (variantRaw.parameters != null) {
                                for (RawParameter paramRaw : variantRaw.parameters) {
                                    DocVariable param = new DocVariable(paramRaw.name);
                                    param.type = paramRaw.type;
                                    param.doc = paramRaw.docs == null ? "" : paramRaw.docs;
                                    variant.params.add(param);
                                }
                            }

                            if (variantRaw.annotations != null) {
                                variant.annotations.addAll(variantRaw.annotations);
                                if (variant.annotations.contains("java.lang.Deprecated"))
                                    variant.isDeprecated = true;
                            }
                            if (!variant.isDeprecated) method.isDeprecated = false;

                            variant.setParent(method);
                        }

                        if (method.removeDiscardMethods()) {
                            method.setParent(docEntry);
                            methods.add(method);
                        }
                    }
                }

                classes.add(docEntry);
            }

        } catch (Exception e) {
            Mappet.loggerClient.error("Failed to load Docs: {}", e.getMessage());
        }
    }

    public DocEntry getClass(String name) {
        for (DocEntry docClass : classes) {
            if (docClass.name.endsWith(name)) return docClass;
        }

        return null;
    }

    public void remove(String name) {
        classes.removeIf(clazz -> clazz.name.endsWith(name));
    }

    public void copyMethods(String from, String... to) {
        DocEntry source = getClass(from);
        if (source == null) return;
        for (String string : to) {
            DocEntry target = getClass(string);
            if (target != null) {
                for (DocEntry entry : source.entries) {
                    target.addChildren(entry);
                }
            }
        }
    }

    public static class RawClassEntry {
        public String name;
        public String docs;
        public Map<String, List<RawMethodVariant>> methods;
    }

    public static class RawMethodVariant {
        @com.google.gson.annotations.SerializedName("return")
        public RawReturn returns;
        public List<RawParameter> parameters;
        public List<String> annotations;
        public String docs;
    }

    public static class RawReturn {
        public String type;
        public String docs;
    }

    public static class RawParameter {
        public String name;
        public String type;
        public String docs;
    }
}