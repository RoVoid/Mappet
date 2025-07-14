package mchorse.mappet.client.gui.scripts.style;

import net.minecraft.nbt.NBTTagCompound;

public class SyntaxStyle {
    public String title = "Monokai";
    public boolean shadow = true;

    public int keywords = 0xf92672;
    public int constants = 0x66d9ef;
    public int identifiers = 0xa6e22e;
    public int special = 0xfd971f;       // Math, JSON, this, mappet
    public int strings = 0xe6db74;
    public int comments = 0x75715e;
    public int numbers = 0xae81ff;
    public int functions = 0x61d9fa;
    public int methods = 0xa6e22e;
    public int operators = 0xf92672;
    public int other = 0xffffff;

    public int lineNumbers = 0x90918b;
    public int background = 0x282923;

    public SyntaxStyle() {
    }

    public SyntaxStyle(NBTTagCompound tag) {
        fromNBT(tag);
    }

    public SyntaxStyle(String title, boolean shadow,
                       int keywords, int constants, int identifiers, int special,
                       int strings, int comments, int numbers,
                       int functions, int methods, int operators, int other,
                       int lineNumbers, int background) {
        this.title = title;
        this.shadow = shadow;

        this.keywords = keywords;
        this.constants = constants;
        this.identifiers = identifiers;
        this.special = special;
        this.strings = strings;
        this.comments = comments;
        this.numbers = numbers;
        this.functions = functions;
        this.methods = methods;
        this.operators = operators;
        this.other = other;

        this.lineNumbers = lineNumbers;
        this.background = background;
    }

    public NBTTagCompound toNBT() {
        return toNBT(new NBTTagCompound());
    }

    public NBTTagCompound toNBT(NBTTagCompound tag) {
        tag.setString("Title", title);
        tag.setBoolean("Shadow", shadow);

        tag.setInteger("Keywords", keywords);
        tag.setInteger("Constants", constants);
        tag.setInteger("Identifier", identifiers);
        tag.setInteger("Special", special);
        tag.setInteger("Strings", strings);
        tag.setInteger("Comments", comments);
        tag.setInteger("Numbers", numbers);
        tag.setInteger("Functions", functions);
        tag.setInteger("Methods", methods);
        tag.setInteger("Operators", operators);
        tag.setInteger("Other", other);

        tag.setInteger("LineNumbers", lineNumbers);
        tag.setInteger("Background", background);

        return tag;
    }

    public SyntaxStyle fromNBT(NBTTagCompound tag) {
        if (tag == null) return this;

        title = tag.getString("Title");
        shadow = tag.getBoolean("Shadow");

        keywords = tag.getInteger("Keywords");
        constants = tag.getInteger("Constants");
        identifiers = tag.getInteger("Identifier");
        special = tag.getInteger("Special");
        strings = tag.getInteger("Strings");
        comments = tag.getInteger("Comments");
        numbers = tag.getInteger("Numbers");
        functions = tag.getInteger("Functions");
        methods = tag.getInteger("Methods");
        operators = tag.getInteger("Operators");
        other = tag.getInteger("Other");

        lineNumbers = tag.getInteger("LineNumbers");
        background = tag.getInteger("Background");

        return this;
    }
}
