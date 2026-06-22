package com.ldtteam.common.config;

import com.ldtteam.common.platform.EnvUtil;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractConfiguration
{
    public static final String DEFAULT_KEY_PREFIX = "blockui.config.default.";
    public static final String COMMENT_SUFFIX = ".comment";

    final List<ConfigWatcher<?>> watchers = new ArrayList<>();
    private final Builder builder;
    private final String modId;

    private RestartType nextRestartType = RestartType.NONE;

    protected AbstractConfiguration(final Builder builder, final String modId)
    {
        this.builder = builder;
        this.modId = modId;
    }

    void bind(final ConfigBackend backend)
    {
        builder.bind(backend);
    }

    List<ConfigValue<?>> values()
    {
        return builder.values();
    }

    protected void createCategory(final String key)
    {
        if (nextRestartType != RestartType.NONE)
        {
            throw new IllegalStateException("Categories cannot have worldRestart flag!");
        }
        buildBase(key, null).push(key);
    }

    protected void swapToCategory(final String key)
    {
        finishCategory();
        createCategory(key);
    }

    protected void finishCategory()
    {
        builder.pop();
    }

    private String nameTKey(final String key)
    {
        return modId + ".config." + key;
    }

    private String commentTKey(final String key)
    {
        return nameTKey(key) + COMMENT_SUFFIX;
    }

    /**
     * Everything must call this class in the end
     */
    private Builder buildBase(final String key, @Nullable final String defaultDesc)
    {
        switch (nextRestartType)
        {
            case WORLD -> builder.worldRestart();
            case GAME -> builder.gameRestart();
            default -> { }
        }
        nextRestartType = RestartType.NONE;

        String comment = translate(commentTKey(key));
        if (defaultDesc != null && !defaultDesc.isBlank())
        {
            comment += " " + defaultDesc;
        }

        return builder.comment(comment).translation(nameTKey(key));
    }

    private static String translate(final String key, final Object... args)
    {
        return args.length == 0 ? key : key.formatted(args);
    }

    protected AbstractConfiguration requiresWorldRestart()
    {
        return requires(RestartType.WORLD);
    }

    protected AbstractConfiguration requiresGameRestart()
    {
        return requires(RestartType.GAME);
    }

    protected AbstractConfiguration requires(final RestartType restartType)
    {
        nextRestartType = restartType;
        return this;
    }

    protected BooleanValue defineBoolean(final String key, final boolean defaultValue)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "boolean", defaultValue)).define(key, defaultValue);
    }

    protected IntValue defineInteger(final String key, final int defaultValue)
    {
        return defineInteger(key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected IntValue defineInteger(final String key, final int defaultValue, final int min, final int max)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    protected ConfigValue<String> defineString(final String key, final String defaultValue)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "string", defaultValue)).define(key, defaultValue);
    }

    protected LongValue defineLong(final String key, final long defaultValue)
    {
        return defineLong(key, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected LongValue defineLong(final String key, final long defaultValue, final long min, final long max)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    protected DoubleValue defineDouble(final String key, final double defaultValue)
    {
        return defineDouble(key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected DoubleValue defineDouble(final String key, final double defaultValue, final double min, final double max)
    {
        return buildBase(key, translate(DEFAULT_KEY_PREFIX + "number", defaultValue, min, max))
            .defineInRange(key, defaultValue, min, max);
    }

    /**
     * @deprecated kept for source compatibility
     * @see #defineList(String, Supplier, Predicate, List)
     */
    @Deprecated(since = "1.21")
    protected <T> ConfigValue<List<? extends T>> defineList(final String key,
        final List<? extends T> defaultValue,
        final Predicate<Object> elementValidator)
    {
        return buildBase(key, null).defineList(key, defaultValue, elementValidator);
    }

    protected <T> ConfigValue<List<? extends T>> defineList(final String key,
        final Supplier<T> newUiInstance,
        final Predicate<Object> elementValidator,
        final List<? extends T> defaultValue)
    {
        return buildBase(key, null).defineList(key, defaultValue, newUiInstance, elementValidator);
    }

    @SuppressWarnings("unchecked")
    protected <T> ConfigValue<List<? extends T>> defineList(final String key,
        final Supplier<T> newUiInstance,
        final Predicate<Object> elementValidator,
        final T... values)
    {
        return buildBase(key, null).defineList(key, () -> List.of(values), newUiInstance, elementValidator);
    }

    /**
     * @deprecated kept for source compatibility
     * @see #defineListAllowEmpty(String, Supplier, Predicate, List)
     */
    @Deprecated(since = "1.21")
    protected <T> ConfigValue<List<? extends T>> defineListAllowEmpty(final String key,
        final List<? extends T> defaultValue,
        final Predicate<Object> elementValidator)
    {
        return buildBase(key, null).defineListAllowEmpty(key, defaultValue, elementValidator);
    }

    protected <T> ConfigValue<List<? extends T>> defineListAllowEmpty(final String key,
        final Supplier<T> newUiInstance,
        final Predicate<Object> elementValidator,
        final List<? extends T> defaultValue)
    {
        return buildBase(key, null).defineListAllowEmpty(key, defaultValue, newUiInstance, elementValidator);
    }

    @SuppressWarnings("unchecked")
    protected <T> ConfigValue<List<? extends T>> defineListAllowEmpty(final String key,
        final Supplier<T> newUiInstance,
        final Predicate<Object> elementValidator,
        final T... values)
    {
        return buildBase(key, null).defineListAllowEmpty(key, () -> List.of(values), newUiInstance, elementValidator);
    }

    protected <V extends Enum<V>> EnumValue<V> defineEnum(final String key, final V defaultValue)
    {
        return buildBase(key,
            translate(DEFAULT_KEY_PREFIX + "enum",
                defaultValue,
                Arrays.stream(defaultValue.getDeclaringClass().getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "))))
            .defineEnum(key, defaultValue);
    }

    protected <T> void addWatcher(final ConfigValue<T> configValue, final ConfigListener<T> listener)
    {
        watchers.add(new ConfigWatcher<>(listener, configValue));
    }

    @SuppressWarnings("unchecked")
    protected void addWatcher(final Runnable listener, final ConfigValue<?>... configValues)
    {
        final ConfigListener<Object> typedListener = (o, n) -> listener.run();
        for (final ConfigValue<?> c : configValues)
        {
            watchers.add(new ConfigWatcher<>(typedListener, (ConfigValue<Object>) c));
        }
    }

    @FunctionalInterface
    public interface ConfigListener<T>
    {
        void onChange(T oldValue, T newValue);
    }

    public enum RestartType
    {
        NONE,
        WORLD,
        GAME
    }

    public static class ValueSpec
    {
        private final String path;
        private final String translationKey;
        private final String comment;
        private final Object defaultValue;
        private final Predicate<Object> validator;
        private final Predicate<Object> elementValidator;
        private final Supplier<?> newUiInstance;
        private final RestartType restartType;
        private final Class<? extends Enum<?>> enumClass;
        private final Number min;
        private final Number max;
        private final boolean allowEmpty;

        ValueSpec(
            final String path,
            final String translationKey,
            final String comment,
            final Object defaultValue,
            final Predicate<Object> validator,
            final Predicate<Object> elementValidator,
            final Supplier<?> newUiInstance,
            final RestartType restartType,
            final Class<? extends Enum<?>> enumClass,
            final Number min,
            final Number max,
            final boolean allowEmpty)
        {
            this.path = path;
            this.translationKey = translationKey;
            this.comment = comment;
            this.defaultValue = defaultValue;
            this.validator = validator;
            this.elementValidator = elementValidator;
            this.newUiInstance = newUiInstance;
            this.restartType = restartType;
            this.enumClass = enumClass;
            this.min = min;
            this.max = max;
            this.allowEmpty = allowEmpty;
        }

        public String getPath()
        {
            return path;
        }

        public String getTranslationKey()
        {
            return translationKey;
        }

        public String getComment()
        {
            return comment;
        }

        public Object getDefaultValue()
        {
            return defaultValue;
        }

        public Predicate<Object> getValidator()
        {
            return validator;
        }

        public Predicate<Object> getElementValidator()
        {
            return elementValidator;
        }

        public Supplier<?> getNewUiInstance()
        {
            return newUiInstance;
        }

        public RestartType getRestartType()
        {
            return restartType;
        }

        public Class<? extends Enum<?>> getEnumClass()
        {
            return enumClass;
        }

        public Number getMin()
        {
            return min;
        }

        public Number getMax()
        {
            return max;
        }

        public boolean allowsEmpty()
        {
            return allowEmpty;
        }
    }

    public static class ConfigValue<T>
    {
        private final ValueSpec spec;
        private final T defaultValue;
        private T value;
        private ConfigBackend backend;

        ConfigValue(final ValueSpec spec, final T defaultValue)
        {
            this.spec = spec;
            this.defaultValue = copyValue(defaultValue);
            this.value = copyValue(defaultValue);
        }

        public T get()
        {
            return copyValue(value);
        }

        public void set(final T value)
        {
            this.value = sanitizeValue(value);
            if (backend != null)
            {
                backend.set(this, this.value);
            }
        }

        public void save()
        {
            if (backend != null)
            {
                backend.save();
            }
        }

        public ValueSpec getSpec()
        {
            return spec;
        }

        public T getDefault()
        {
            return copyValue(defaultValue);
        }

        void bind(final ConfigBackend backend)
        {
            this.backend = backend;
        }

        void loadFromBackend(final Object loadedValue)
        {
            if (loadedValue != null)
            {
                this.value = sanitizeValue((T) loadedValue);
            }
            else
            {
                this.value = copyValue(defaultValue);
            }
        }

        Object serialize()
        {
            final T current = value;
            if (current instanceof Enum<?> enumValue)
            {
                return enumValue.name();
            }
            if (current instanceof List<?> list)
            {
                return new ArrayList<>(list);
            }
            return current;
        }

        @SuppressWarnings("unchecked")
        private T sanitizeValue(final T incoming)
        {
            if (incoming == null)
            {
                return copyValue(defaultValue);
            }

            if (incoming instanceof List<?> list)
            {
                final List<Object> sanitized = new ArrayList<>();
                for (final Object element : list)
                {
                    if (spec.elementValidator == null || spec.elementValidator.test(element))
                    {
                        sanitized.add(element);
                    }
                }
                if (!spec.allowEmpty && sanitized.isEmpty() && defaultValue instanceof List<?> defaults)
                {
                    return (T) new ArrayList<>(defaults);
                }
                return (T) sanitized;
            }

            if (incoming instanceof Number number)
            {
                if (incoming instanceof Integer)
                {
                    int value = number.intValue();
                    if (spec.min != null)
                    {
                        value = Math.max(spec.min.intValue(), value);
                    }
                    if (spec.max != null)
                    {
                        value = Math.min(spec.max.intValue(), value);
                    }
                    return (T) Integer.valueOf(value);
                }
                if (incoming instanceof Long)
                {
                    long value = number.longValue();
                    if (spec.min != null)
                    {
                        value = Math.max(spec.min.longValue(), value);
                    }
                    if (spec.max != null)
                    {
                        value = Math.min(spec.max.longValue(), value);
                    }
                    return (T) Long.valueOf(value);
                }
                if (incoming instanceof Double)
                {
                    double value = number.doubleValue();
                    if (spec.min != null)
                    {
                        value = Math.max(spec.min.doubleValue(), value);
                    }
                    if (spec.max != null)
                    {
                        value = Math.min(spec.max.doubleValue(), value);
                    }
                    return (T) Double.valueOf(value);
                }
            }

            if (spec.validator != null && !spec.validator.test(incoming))
            {
                return copyValue(defaultValue);
            }
            return copyValue(incoming);
        }

        @SuppressWarnings("unchecked")
        private T copyValue(final T input)
        {
            if (input instanceof List<?> list)
            {
                return (T) new ArrayList<>(list);
            }
            return input;
        }
    }

    public static final class BooleanValue extends ConfigValue<Boolean>
    {
        BooleanValue(final ValueSpec spec, final Boolean defaultValue)
        {
            super(spec, defaultValue);
        }
    }

    public static final class IntValue extends ConfigValue<Integer>
    {
        IntValue(final ValueSpec spec, final Integer defaultValue)
        {
            super(spec, defaultValue);
        }
    }

    public static final class LongValue extends ConfigValue<Long>
    {
        LongValue(final ValueSpec spec, final Long defaultValue)
        {
            super(spec, defaultValue);
        }
    }

    public static final class DoubleValue extends ConfigValue<Double>
    {
        DoubleValue(final ValueSpec spec, final Double defaultValue)
        {
            super(spec, defaultValue);
        }
    }

    public static final class EnumValue<V extends Enum<V>> extends ConfigValue<V>
    {
        EnumValue(final ValueSpec spec, final V defaultValue)
        {
            super(spec, defaultValue);
        }
    }

    public static class Builder
    {
        private final List<String> categoryStack = new ArrayList<>();
        private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();

        private String pendingComment = "";
        private String pendingTranslation = "";
        private RestartType pendingRestartType = RestartType.NONE;

        public <T extends AbstractConfiguration> SimplePair<T, Builder> configure(final Function<Builder, T> factory)
        {
            final T configuration = factory.apply(this);
            return new SimplePair<>(configuration, this);
        }

        public Builder push(final String name)
        {
            categoryStack.add(name);
            return this;
        }

        public Builder pop()
        {
            if (!categoryStack.isEmpty())
            {
                categoryStack.remove(categoryStack.size() - 1);
            }
            return this;
        }

        public Builder comment(final String comment)
        {
            pendingComment = comment;
            return this;
        }

        public Builder translation(final String translationKey)
        {
            pendingTranslation = translationKey;
            return this;
        }

        public Builder worldRestart()
        {
            pendingRestartType = RestartType.WORLD;
            return this;
        }

        public Builder gameRestart()
        {
            pendingRestartType = RestartType.GAME;
            return this;
        }

        public BooleanValue define(final String key, final boolean defaultValue)
        {
            return register(new BooleanValue(nextSpec(key, defaultValue, Boolean.class::isInstance, null, null, null, false), defaultValue));
        }

        public ConfigValue<String> define(final String key, final String defaultValue)
        {
            return register(new ConfigValue<>(nextSpec(key, defaultValue, String.class::isInstance, null, null, null, false), defaultValue));
        }

        public IntValue defineInRange(final String key, final int defaultValue, final int min, final int max)
        {
            return register(new IntValue(nextSpec(key, defaultValue, Number.class::isInstance, null, min, max, false), defaultValue));
        }

        public LongValue defineInRange(final String key, final long defaultValue, final long min, final long max)
        {
            return register(new LongValue(nextSpec(key, defaultValue, Number.class::isInstance, null, min, max, false), defaultValue));
        }

        public DoubleValue defineInRange(final String key, final double defaultValue, final double min, final double max)
        {
            return register(new DoubleValue(nextSpec(key, defaultValue, Number.class::isInstance, null, min, max, false), defaultValue));
        }

        public <T> ConfigValue<List<? extends T>> defineList(final String key,
            final List<? extends T> defaultValue,
            final Predicate<Object> elementValidator)
        {
            return defineList(key, () -> null, elementValidator, defaultValue, false);
        }

        public <T> ConfigValue<List<? extends T>> defineList(final String key,
            final List<? extends T> defaultValue,
            final Supplier<T> newUiInstance,
            final Predicate<Object> elementValidator)
        {
            return defineList(key, newUiInstance, elementValidator, defaultValue, false);
        }

        public <T> ConfigValue<List<? extends T>> defineList(final String key,
            final Supplier<List<? extends T>> defaultValue,
            final Supplier<T> newUiInstance,
            final Predicate<Object> elementValidator)
        {
            return defineList(key, newUiInstance, elementValidator, defaultValue.get(), false);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(final String key,
            final List<? extends T> defaultValue,
            final Predicate<Object> elementValidator)
        {
            return defineList(key, () -> null, elementValidator, defaultValue, true);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(final String key,
            final List<? extends T> defaultValue,
            final Supplier<T> newUiInstance,
            final Predicate<Object> elementValidator)
        {
            return defineList(key, newUiInstance, elementValidator, defaultValue, true);
        }

        public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(final String key,
            final Supplier<List<? extends T>> defaultValue,
            final Supplier<T> newUiInstance,
            final Predicate<Object> elementValidator)
        {
            return defineList(key, newUiInstance, elementValidator, defaultValue.get(), true);
        }

        public <V extends Enum<V>> EnumValue<V> defineEnum(final String key, final V defaultValue)
        {
            return register(new EnumValue<>(
                nextSpec(key, defaultValue, value -> value instanceof String || defaultValue.getDeclaringClass().isInstance(value), null, null, null, false, null, defaultValue.getDeclaringClass()),
                defaultValue));
        }

        void bind(final ConfigBackend backend)
        {
            values.values().forEach(value -> value.bind(backend));
        }

        List<ConfigValue<?>> values()
        {
            return new ArrayList<>(values.values());
        }

        private <T> ConfigValue<List<? extends T>> defineList(
            final String key,
            final Supplier<T> newUiInstance,
            final Predicate<Object> elementValidator,
            final List<? extends T> defaultValue,
            final boolean allowEmpty)
        {
            return register(new ConfigValue<>(
                nextSpec(key, new ArrayList<>(defaultValue), List.class::isInstance, elementValidator, null, null, allowEmpty, newUiInstance, null),
                new ArrayList<>(defaultValue)));
        }

        private ValueSpec nextSpec(
            final String key,
            final Object defaultValue,
            final Predicate<Object> validator,
            final Predicate<Object> elementValidator,
            final Number min,
            final Number max,
            final boolean allowEmpty)
        {
            return nextSpec(key, defaultValue, validator, elementValidator, min, max, allowEmpty, null, null);
        }

        private ValueSpec nextSpec(
            final String key,
            final Object defaultValue,
            final Predicate<Object> validator,
            final Predicate<Object> elementValidator,
            final Number min,
            final Number max,
            final boolean allowEmpty,
            final Supplier<?> newUiInstance,
            final Class<? extends Enum<?>> enumClass)
        {
            final ValueSpec spec = new ValueSpec(
                fullPath(key),
                pendingTranslation,
                pendingComment,
                defaultValue,
                validator,
                elementValidator,
                newUiInstance,
                pendingRestartType,
                enumClass,
                min,
                max,
                allowEmpty);
            pendingComment = "";
            pendingTranslation = "";
            pendingRestartType = RestartType.NONE;
            return spec;
        }

        private String fullPath(final String key)
        {
            if (categoryStack.isEmpty())
            {
                return key;
            }
            return String.join(".", categoryStack) + "." + key;
        }

        private <T extends ConfigValue<?>> T register(final T value)
        {
            values.put(value.getSpec().getPath(), value);
            return value;
        }
    }

    /**
     * synchronized due to nature of config events
     */
    static class ConfigWatcher<T>
    {
        private final ConfigListener<T> listener;
        private final ConfigValue<T> configValue;

        @Nullable
        private T lastValue;

        private ConfigWatcher(final ConfigListener<T> listener, final ConfigValue<T> configValue)
        {
            this.listener = listener;
            this.configValue = configValue;
        }

        boolean isSameForgeConfig(final ConfigValue<?> other)
        {
            return other == configValue;
        }

        synchronized void cacheLastValue()
        {
            lastValue = configValue.get();
        }

        synchronized void compareAndFireChangeEvent()
        {
            final T newValue = configValue.get();

            if (!Objects.equals(newValue, lastValue))
            {
                final T oldValue = lastValue;
                if (EnvUtil.isClient())
                {
                    Minecraft.getInstance().execute(() -> listener.onChange(oldValue, newValue));
                }
                else
                {
                    listener.onChange(oldValue, newValue);
                }
                lastValue = newValue;
            }
        }
    }
}
