package com.cleanroommc.modularui.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Helper for looking up game resources through the client resource manager.
 */
public class AssetHelper {

    /**
     * Looks up a single resource.
     *
     * @param resourceLocation location of the resource
     * @return the resource, or {@code null} if it can't be loaded
     */
    public static @Nullable IResource findAsset(ResourceLocation resourceLocation) {
        try {
            return Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Collects every resource at the given domain and path, including those from resource packs.
     *
     * @param domain domain (namespace) of the resource
     * @param file file path inside the domain
     * @return all matching resources, or an empty list on failure
     */
    public static List<IResource> findAssets(String domain, String file) {
        try {
            return Minecraft.getMinecraft().getResourceManager().getAllResources(new ResourceLocation(domain, file));
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Searches for the file in every resource domain currently loaded by the resource manager.
     *
     * @param file file path inside each domain
     * @return all matching resources across all domains, possibly empty
     */
    public static List<IResource> findAssets(String file) {
        ObjectList<IResource> assets = ObjectList.create();
        for (String domain : Minecraft.getMinecraft().getResourceManager().getResourceDomains()) {
            assets.addAll(findAssets(domain, file));
        }
        return assets;
    }
}
