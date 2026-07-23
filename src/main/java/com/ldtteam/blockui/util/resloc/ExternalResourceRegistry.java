package com.ldtteam.blockui.util.resloc;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Sidecar registry replacing the old OutOfJarResourceLocation subclassing pattern.
 */
public final class ExternalResourceRegistry
{
    private static final Map<Identifier, Path> PATHS = new ConcurrentHashMap<>();

    private ExternalResourceRegistry()
    {
    }

    public static Identifier register(final String namespace, final Path path)
    {
        final String pathString = path.toString().toLowerCase().replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_");
        final Identifier id = Identifier.fromNamespaceAndPath(namespace, pathString);
        PATHS.put(id, path);
        return id;
    }

    @SuppressWarnings("resource")
    public static Identifier ofMinecraftFolder(final String namespace, final String... parts)
    {
        Path path = Minecraft.getInstance().gameDirectory.toPath().resolve(namespace);
        for (final String part : parts)
        {
            path = path.resolve(part);
        }
        return register(namespace, path);
    }

    public static CompletableFuture<@Nullable Identifier> ofMinecraftSkin(final Minecraft minecraft,
        final GameProfile gameProfile,
        @Nullable final Function<Object, Identifier> textureSelector)
    {
        return minecraft.getSkinManager().get(gameProfile).thenApply(skinOpt ->
            skinOpt.map(skin -> {
                final Object asset;
                if (textureSelector != null)
                {
                    final Identifier selected = textureSelector.apply(skin);
                    if (selected != null)
                    {
                        return selected;
                    }
                }
                asset = skin.body();
                if (asset instanceof final net.minecraft.core.ClientAsset.Texture tex)
                {
                    return tex.texturePath();
                }
                return null;
            }).orElse(null));
    }

    public static boolean isExternal(final Identifier id)
    {
        return PATHS.containsKey(id);
    }

    public static @Nullable Path getPath(final Identifier id)
    {
        return PATHS.get(id);
    }

    public static boolean fileExists(final Identifier id, final ResourceManager fallback)
    {
        final Path path = PATHS.get(id);
        return path != null ? Files.exists(path) : fallback.getResource(id).isPresent();
    }

    public static Resource getResourceHandle(final Identifier id, final ResourceManager fallback) throws IOException
    {
        final Path path = PATHS.get(id);
        if (path != null)
        {
            return new ExternalResource(id, path);
        }
        return fallback.getResource(id).orElseThrow(() -> new FileNotFoundException("File not found: " + id));
    }

    public static InputStream openStream(final Identifier id, final ResourceManager fallback) throws IOException
    {
        final Path path = PATHS.get(id);
        return path != null ? Files.newInputStream(path) : fallback.open(id);
    }

    public static BufferedReader openReader(final Identifier id, final ResourceManager fallback) throws IOException
    {
        final Path path = PATHS.get(id);
        return path != null ? Files.newBufferedReader(path) : fallback.openAsReader(id);
    }

    public static final class ExternalResource extends Resource
    {
        private final Identifier id;

        public ExternalResource(final Identifier id, final Path path)
        {
            super(null, IoSupplier.create(path), ResourceMetadata.EMPTY_SUPPLIER);
            this.id = id;
        }

        @Override
        public PackResources source()
        {
            return null;
        }

        @Override
        public String sourcePackId()
        {
            return "blockui external resource: " + id;
        }
    }
}
