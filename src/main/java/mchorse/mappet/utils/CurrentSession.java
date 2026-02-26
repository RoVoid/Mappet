package mchorse.mappet.utils;

import mchorse.mappet.api.utils.content.IContentTypeBase;

import java.util.Objects;

public class CurrentSession
{
    public IContentTypeBase type;
    public String id = "";

    public IContentTypeBase activeType;
    public String activeId = "";

    public void set(IContentTypeBase type, String id)
    {
        this.type = type;
        this.id = id;
    }

    public void setActive(IContentTypeBase type, String id)
    {
        this.activeType = type;
        this.activeId = id;
    }

    public void reset()
    {
        this.set(null, "");
        this.setActive(null, "");
    }

    public boolean isEditing(IContentTypeBase type, String id)
    {
        return this.type == type && Objects.equals(this.id, id);
    }

    public boolean isActive(IContentTypeBase type, String id)
    {
        return this.activeType == type && Objects.equals(this.activeId, id);
    }
}