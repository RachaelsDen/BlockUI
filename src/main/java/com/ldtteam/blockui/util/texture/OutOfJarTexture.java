package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.ExternalResourceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * 26.2 compatibility shim.
 * Full external texture loading is deferred; this utility now only resolves already-registered textures or the missing texture.
 */
public final class OutOfJarTexture
{
    private OutOfJarTexture()
    {
    }

    public static AbstractTexture assertLoadedDefaultManagers(final Identifier resLoc)
    {
        return assertLoaded(resLoc, Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getResourceManager());
    }

    public static AbstractTexture assertLoaded(final Identifier resLoc, final TextureManager textureManager, final ResourceManager resourceManager)
    {
        if (!ExternalResourceRegistry.isExternal(resLoc))
        {
            return textureManager.getTexture(resLoc);
        }

        return textureManager.getTexture(MissingTextureAtlasSprite.getLocation());
    }
}
