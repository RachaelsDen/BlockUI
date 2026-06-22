package com.ldtteam.common.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

interface IEventBus
{
    default <T> void addListener(final Class<T> type, final Consumer<T> listener)
    {
    }
}

interface ModContainer
{
    default String getModId()
    {
        return "blockui";
    }

    default void registerConfigScreenFactory(final Function<Screen, Screen> factory)
    {
    }
}

final class ModConfig
{
    enum Type
    {
        CLIENT,
        SERVER,
        COMMON
    }

    private final Type type;
    private final ConfigBackend backend;

    ModConfig(final Type type, final ConfigBackend backend)
    {
        this.type = type;
        this.backend = backend;
    }

    Type getType()
    {
        return type;
    }

    ConfigBackend getSpec()
    {
        return backend;
    }
}

final class SimplePair<L, R>
{
    private final L left;
    private final R right;

    SimplePair(final L left, final R right)
    {
        this.left = left;
        this.right = right;
    }

    L getLeft()
    {
        return left;
    }

    R getRight()
    {
        return right;
    }
}

final class ConfigBackend
{
    private final String modId;
    private final ModConfig.Type type;
    private final Path path;
    private final Map<String, AbstractConfiguration.ConfigValue<?>> values = new LinkedHashMap<>();
    private long lastKnownWrite = -1L;

    ConfigBackend(final String modId, final ModConfig.Type type, final Collection<AbstractConfiguration.ConfigValue<?>> configValues)
    {
        this.modId = modId;
        this.type = type;
        this.path = FabricLoader.getInstance().getConfigDir().resolve(modId + ".toml");
        for (final AbstractConfiguration.ConfigValue<?> value : configValues)
        {
            values.put(value.getSpec().getPath(), value);
        }
    }

    Path path()
    {
        return path;
    }

    void load()
    {
        final Map<String, String> document = readDocument();
        for (final AbstractConfiguration.ConfigValue<?> value : values.values())
        {
            value.loadFromBackend(parseValue(value.getSpec(), document.get(storageKey(value.getSpec().getPath()))));
        }
    }

    void reloadIfChanged()
    {
        try
        {
            if (Files.exists(path))
            {
                final long modified = Files.getLastModifiedTime(path).toMillis();
                if (modified != lastKnownWrite)
                {
                    load();
                    lastKnownWrite = modified;
                }
            }
        }
        catch (final IOException ignored)
        {
        }
    }

    void save()
    {
        try
        {
            Files.createDirectories(path.getParent());
            final Map<String, String> document = readDocument();
            for (final AbstractConfiguration.ConfigValue<?> value : values.values())
            {
                document.put(storageKey(value.getSpec().getPath()), renderValue(value.serialize()));
            }
            Files.writeString(path, renderDocument(document), StandardCharsets.UTF_8);
            lastKnownWrite = Files.getLastModifiedTime(path).toMillis();
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Failed to save config to " + path, e);
        }
    }

    <T> void set(final AbstractConfiguration.ConfigValue<T> value, final T newValue)
    {
        values.put(value.getSpec().getPath(), value);
    }

    private String storageKey(final String path)
    {
        return type.name().toLowerCase() + "." + path;
    }

    private Map<String, String> readDocument()
    {
        final Map<String, String> parsed = new LinkedHashMap<>();
        if (!Files.exists(path))
        {
            return parsed;
        }

        try
        {
            String section = "";
            for (final String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8))
            {
                final String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]"))
                {
                    section = line.substring(1, line.length() - 1).trim();
                    continue;
                }

                final int separator = line.indexOf('=');
                if (separator < 0)
                {
                    continue;
                }

                final String key = line.substring(0, separator).trim();
                final String value = line.substring(separator + 1).trim();
                parsed.put(section.isEmpty() ? key : section + "." + key, value);
            }
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Failed to read config from " + path, e);
        }
        return parsed;
    }

    private String renderDocument(final Map<String, String> document)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("# ").append(modId).append(" configuration\n");

        String currentSection = null;
        for (final Map.Entry<String, String> entry : document.entrySet())
        {
            final int split = entry.getKey().lastIndexOf('.');
            final String section = split < 0 ? "" : entry.getKey().substring(0, split);
            final String key = split < 0 ? entry.getKey() : entry.getKey().substring(split + 1);
            if (!Objects.equals(currentSection, section))
            {
                if (builder.length() > 0)
                {
                    builder.append('\n');
                }
                if (!section.isEmpty())
                {
                    builder.append('[').append(section).append("]\n");
                }
                currentSection = section;
            }
            builder.append(key).append(" = ").append(entry.getValue()).append('\n');
        }
        return builder.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object parseValue(final AbstractConfiguration.ValueSpec spec, final String raw)
    {
        if (raw == null)
        {
            return null;
        }

        final Object defaultValue = spec.getDefaultValue();
        if (defaultValue instanceof Boolean)
        {
            return Boolean.parseBoolean(raw);
        }
        if (defaultValue instanceof Integer)
        {
            return Integer.parseInt(raw);
        }
        if (defaultValue instanceof Long)
        {
            return Long.parseLong(raw);
        }
        if (defaultValue instanceof Double)
        {
            return Double.parseDouble(raw);
        }
        if (defaultValue instanceof Enum<?> && spec.getEnumClass() != null)
        {
            final String cleaned = unquote(raw);
            return Enum.valueOf((Class<? extends Enum>) spec.getEnumClass(), cleaned);
        }
        if (defaultValue instanceof List<?> list)
        {
            return parseList(raw, list.isEmpty() ? null : list.getFirst());
        }
        return unquote(raw);
    }

    private List<Object> parseList(final String raw, final Object sample)
    {
        final String trimmed = raw.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]"))
        {
            return new ArrayList<>();
        }
        final String content = trimmed.substring(1, trimmed.length() - 1).trim();
        if (content.isEmpty())
        {
            return new ArrayList<>();
        }

        final List<Object> parsed = new ArrayList<>();
        for (final String token : splitList(content))
        {
            if (sample instanceof Integer)
            {
                parsed.add(Integer.parseInt(token));
            }
            else if (sample instanceof Long)
            {
                parsed.add(Long.parseLong(token));
            }
            else if (sample instanceof Double)
            {
                parsed.add(Double.parseDouble(token));
            }
            else if (sample instanceof Boolean)
            {
                parsed.add(Boolean.parseBoolean(token));
            }
            else
            {
                parsed.add(unquote(token));
            }
        }
        return parsed;
    }

    private List<String> splitList(final String content)
    {
        final List<String> tokens = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < content.length(); i++)
        {
            final char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\'))
            {
                quoted = !quoted;
            }
            if (c == ',' && !quoted)
            {
                tokens.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        tokens.add(current.toString().trim());
        return tokens;
    }

    private String renderValue(final Object value)
    {
        if (value instanceof String stringValue)
        {
            return quote(stringValue);
        }
        if (value instanceof Enum<?> enumValue)
        {
            return quote(enumValue.name());
        }
        if (value instanceof List<?> list)
        {
            return list.stream().map(this::renderValue).collect(java.util.stream.Collectors.joining(", ", "[", "]"));
        }
        return String.valueOf(value);
    }

    private String quote(final String raw)
    {
        return '"' + raw.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

    private String unquote(final String raw)
    {
        final String trimmed = raw.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\""))
        {
            return trimmed.substring(1, trimmed.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return trimmed;
    }
}
