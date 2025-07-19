package mchorse.mappet.client.gui.scripts.utils.documentation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mchorse.mappet.Mappet;
import mchorse.mappet.client.gui.scripts.GuiDocumentationOverlayPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;

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
        Language language = mc.getLanguageManager().getCurrentLanguage();
        Gson gson = new GsonBuilder().create();

        String path = "/assets/mappet/docs/" + language.getLanguageCode() + ".json";
        InputStream stream = GuiDocumentationOverlayPanel.class.getResourceAsStream(path);

        if (stream == null) {
            stream = GuiDocumentationOverlayPanel.class.getResourceAsStream("/assets/mappet/docs/en_US.json");
        }

        if (stream != null) {
            try (Scanner scanner = new Scanner(stream, "UTF-8")) {
                String json = scanner.useDelimiter("\\A").next();

                Type type = new TypeToken<Map<String, RawClassEntry>>() {
                }.getType();
                Map<String, RawClassEntry> rawDocs = gson.fromJson(json, type);

                for (Map.Entry<String, RawClassEntry> entry : rawDocs.entrySet()) {
                    RawClassEntry raw = entry.getValue();
                    DocEntry docEntry = new DocEntry(entry.getKey());
                    docEntry.doc = raw.docs != null ? raw.docs : "";

                    if (raw.methods != null) {
                        for (Map.Entry<String, List<RawMethodVariant>> m : raw.methods.entrySet()) {
                            String methodName = m.getKey();
                            List<RawMethodVariant> variants = m.getValue();

                            DocMethod method = new DocMethod(methodName);

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
                                        DocVariable param = new DocVariable();
                                        param.name = paramRaw.name;
                                        param.type = paramRaw.type;
                                        param.doc = paramRaw.docs != null ? paramRaw.docs : "";
                                        variant.params.add(param);
                                    }
                                }

                                if (variantRaw.annotations != null) {
                                    variant.annotations.addAll(variantRaw.annotations);
                                }

                                method.addChildren(variant);
                            }

                            docEntry.addChildren(method);
                            this.methods.add(method);
                        }
                    }

                    this.classes.add(docEntry);
                }

            } catch (Exception e) {
                Mappet.logger.error("Failed to load Docs: " + e.getMessage());
            }
        }
    }

    public DocEntry getClass(String name) {
        for (DocEntry docClass : this.classes) {
            if (docClass.name.endsWith(name)) {
                return docClass;
            }
        }

        return null;
    }

    public void remove(String name) {
        this.classes.removeIf(clazz -> clazz.name.endsWith(name));
    }

    public void copyMethods(String from, String... to) {
        DocEntry source = this.getClass(from);

        if (source == null) return;

        for (String string : to) {
            DocEntry target = this.getClass(string);
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