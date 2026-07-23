package com.ldtteam.blockui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;

/**
 * Provides access to the GUI sprite atlas and sprite scaling metadata.
 * Custom mod atlas registration is handled via RegisterTextureAtlasesEvent in ClientLifecycleSubscriber.
 */
public class AtlasManager
{
    public static final AtlasManager INSTANCE = new AtlasManager();

    private AtlasManager()
    {
    }

    public void addAtlas(final java.util.function.Consumer<net.minecraft.server.packs.resources.PreparableReloadListener> resourceRegistry, final String modId)
    {
        // no-op: atlas registration is handled by ClientLifecycleSubscriber.onRegisterTextureAtlases
    }

    public TextureAtlasSprite getSprite(final Identifier resLoc)
    {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(resLoc);
    }

    public void dumpAtlases(final Path dumpingFolder)
    {
        // not yet implemented for the 26.2 atlas system
    }

    public static GuiSpriteScaling getSpriteScaling(final TextureAtlasSprite textureAtlasSprite)
    {
        return textureAtlasSprite.contents()
            .getAdditionalMetadata(GuiMetadataSection.TYPE)
            .orElse(GuiMetadataSection.DEFAULT)
            .scaling();
    }
}
