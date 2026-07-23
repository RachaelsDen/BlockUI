package com.ldtteam.blockui.util.texture;

import com.ldtteam.blockui.util.resloc.ExternalResourceRegistry;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility for loading textures from external file paths (outside the jar/resource pack system).
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

        // Check if already registered
        final AbstractTexture existing = textureManager.getTexture(resLoc);
        if (!(existing instanceof MissingHolder))
        {
            return existing;
        }

        // Register and load a file-backed texture
        final Path path = ExternalResourceRegistry.getPath(resLoc);
        if (path != null)
        {
            final FileBackedTexture texture = new FileBackedTexture(resLoc, path);
            textureManager.registerAndLoad(resLoc, texture);
            return texture;
        }

        return textureManager.getTexture(resLoc);
    }

    /**
     * Simple ReloadableTexture that loads a NativeImage from an external file path.
     */
    private static final class FileBackedTexture extends ReloadableTexture
    {
        private final Path filePath;

        FileBackedTexture(final Identifier id, final Path filePath)
        {
            super(id);
            this.filePath = filePath;
        }

        @Override
        public TextureContents loadContents(@Nullable final ResourceManager resourceManager) throws IOException
        {
            try (var is = java.nio.file.Files.newInputStream(filePath))
            {
                final NativeImage image = NativeImage.read(is);
                return new TextureContents(image, new TextureMetadataSection(false, false, net.minecraft.client.renderer.texture.MipmapStrategy.AUTO, 0.0f));
            }
        }
    }

    /**
     * Placeholder used by {@link #assertLoaded} to detect unregistered textures.
     * TextureManager returns this for unknown identifiers.
     */
    private static final class MissingHolder extends AbstractTexture
    {
    }
}
