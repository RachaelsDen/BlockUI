package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.ExternalResourceRegistry;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import java.util.Set;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Dynamic texture based on given sprite info
 */
public class SpriteTexture extends AbstractTexture implements TickableTexture
{
    private final Identifier resourceLocation;

    private SpriteContents sprite;

    /**
     * intentionally out-of-jar ctor, for normal locations use vanilla atlases
     */
    public SpriteTexture(final Identifier resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    public void load(final ResourceManager resourceManager) throws IOException
    {
        // cleanup old data
        close();
        
        if (!ExternalResourceRegistry.fileExists(resourceLocation, resourceManager))
        {
            throw new FileNotFoundException(resourceLocation.toString());
        }

        final Resource resource = ExternalResourceRegistry.getResourceHandle(resourceLocation, resourceManager);

        sprite = SpriteResourceLoader.create(Set.of(AnimationMetadataSection.TYPE)).loadSprite(resourceLocation, resource);
    }

    @Override
    public void tick()
    {
        // deferred: animated sprite ticking needs the 26.2 texture upload path
    }

    @Override
    public void close()
    {
        if (sprite != null)
        {
            sprite.close();
        }
    }
}
