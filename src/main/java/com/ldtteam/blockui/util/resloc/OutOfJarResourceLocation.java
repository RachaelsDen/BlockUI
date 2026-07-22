package com.ldtteam.blockui.util.resloc;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Legacy compatibility facade for the old OutOfJarResourceLocation API.
 * The 26.2 branch uses ExternalResourceRegistry as the real implementation.
 */
public final class OutOfJarResourceLocation
{
    private OutOfJarResourceLocation()
    {
    }

    public static Identifier of(final String namespace, final Path path)
    {
        return ExternalResourceRegistry.register(namespace, path);
    }

    public static Identifier ofMinecraftFolder(final String namespace, final String... parts)
    {
        return ExternalResourceRegistry.ofMinecraftFolder(namespace, parts);
    }

    public static CompletableFuture<@Nullable Identifier> ofMinecraftSkin(final Minecraft minecraft,
        final GameProfile gameProfile,
        @Nullable final Function<Object, Identifier> textureSelector)
    {
        return ExternalResourceRegistry.ofMinecraftSkin(minecraft, gameProfile, textureSelector);
    }

    public static boolean fileExists(final Identifier resLoc, final ResourceManager fallbackManager)
    {
        return ExternalResourceRegistry.fileExists(resLoc, fallbackManager);
    }

    public static Resource getResourceHandle(final Identifier resLoc, final ResourceManager fallbackManager) throws IOException
    {
        return ExternalResourceRegistry.getResourceHandle(resLoc, fallbackManager);
    }

    public static InputStream openStream(final Identifier resLoc, final ResourceManager fallbackManager) throws IOException
    {
        return ExternalResourceRegistry.openStream(resLoc, fallbackManager);
    }

    public static BufferedReader openReader(final Identifier resLoc, final ResourceManager fallbackManager) throws IOException
    {
        return ExternalResourceRegistry.openReader(resLoc, fallbackManager);
    }
}
