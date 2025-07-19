package mchorse.mappet.client.gui.scripts.utils.documentation;

public class DocVariable extends DocEntry
{
    public String type = "";

    public String getType()
    {
        if(type.isEmpty()) return "void";
        int index = type.lastIndexOf(".");
        return index < 0 ? type:type.substring(index + 1);
    }
}