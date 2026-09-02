package mchorse.mappet.utils;

import mchorse.mappet.api.utils.content.IContentTypeBase;

public class CurrentSession
{
    public IContentTypeBase editingType; // edit
    public String editingId = "";

    public IContentTypeBase viewingType; // view
    public String viewingId = "";

    public void hold(IContentTypeBase type, String id)
    {
        editingType = type;
        editingId = id;
        observe(type, id); // if he edits, then of course he watches
    }

    public void observe(IContentTypeBase type, String id)
    {
        viewingType = type;
        viewingId = id;
    }

    public void reset()
    {
        hold(null, "");
        observe(null, "");
    }

    public boolean isEditing(IContentTypeBase type, String id)
    {
        return editingType == type && editingId.equals(id);
    }

    public boolean isActive(IContentTypeBase type, String id)
    {
        return viewingType == type && viewingId.equals(id);
    }

    public boolean isViewing(IContentTypeBase type, String id)
    {
        return viewingType == type && viewingId.equals(id);
    }
}