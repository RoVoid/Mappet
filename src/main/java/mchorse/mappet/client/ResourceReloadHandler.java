package mchorse.mappet.client;

import mchorse.mappet.MappetIcons;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

public class ResourceReloadHandler implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        MappetIcons.initiate();
    }
}
