package com.ldtteam.blockui;

import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiMetadataSection;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * 26.2 compatibility note:
 * custom per-mod gui atlas registration is deferred; this compatibility batch falls back to the vanilla gui sprite path.
 */
public class AtlasManager
{
    public static final AtlasManager INSTANCE = new AtlasManager();

    private AtlasManager()
    {
    }

    public void addAtlas(final Consumer<PreparableReloadListener> resourceRegistry, final String modId)
    {
        // deferred: native atlas registration via RegisterTextureAtlasesEvent / AtlasConfig
    }

    public TextureAtlasSprite getSprite(final Identifier resLoc)
    {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(resLoc);
    }

    public void dumpAtlases(final Path dumpingFolder)
    {
        // deferred: custom atlas dumping depends on the 26.2 atlas registration redesign
    }

    public static GuiSpriteScaling getSpriteScaling(final TextureAtlasSprite textureAtlasSprite)
    {
        return textureAtlasSprite.contents()
            .getAdditionalMetadata(GuiMetadataSection.TYPE)
            .orElse(GuiMetadataSection.DEFAULT)
            .scaling();
    }
}
