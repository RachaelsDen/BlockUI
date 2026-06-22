package com.ldtteam.common.config;

import com.ldtteam.common.config.AbstractConfiguration.BooleanValue;
import com.ldtteam.common.config.AbstractConfiguration.ConfigValue;
import com.ldtteam.common.config.AbstractConfiguration.DoubleValue;
import com.ldtteam.common.config.AbstractConfiguration.EnumValue;
import com.ldtteam.common.config.AbstractConfiguration.IntValue;
import com.ldtteam.common.config.AbstractConfiguration.LongValue;
import com.ldtteam.common.config.AbstractConfiguration.ValueSpec;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Client bouncer class
 */
public class ClientConfigHelper
{
    private static Configurations<?, ?, ?> configurations;

    static void register(final Configurations<?, ?, ?> registeredConfigurations)
    {
        configurations = registeredConfigurations;
    }

    static void registerClient(final ModContainer modContainer)
    {
        if (modContainer != null)
        {
            modContainer.registerConfigScreenFactory(ClientConfigHelper::createScreen);
        }
    }

    private static Screen createScreen(final Screen parent)
    {
        if (configurations == null)
        {
            return parent;
        }

        final ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable(configurations.getModId() + ".config.title"));
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        builder.setSavingRunnable(configurations::saveAll);

        for (final AbstractConfiguration configuration : configurations.activeConfigs())
        {
            final String categoryKey = configuration.getClass().getSimpleName();
            final ConfigCategory category = builder.getOrCreateCategory(Component.literal(categoryKey));
            for (final ConfigValue<?> value : configuration.values())
            {
                addEntry(configurations, category, entryBuilder, value);
            }
        }

        return builder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addEntry(
        final Configurations<?, ?, ?> configurations,
        final ConfigCategory category,
        final ConfigEntryBuilder entryBuilder,
        final ConfigValue<?> value)
    {
        final ValueSpec spec = value.getSpec();
        final Component name = Component.translatable(spec.getTranslationKey().isBlank() ? spec.getPath() : spec.getTranslationKey());
        final Component tooltip = Component.literal(spec.getComment() == null ? "" : spec.getComment());

        if (value instanceof BooleanValue booleanValue)
        {
            category.addEntry(entryBuilder.startBooleanToggle(name, booleanValue.get())
                .setDefaultValue((Boolean) spec.getDefaultValue())
                .setTooltip(tooltip)
                .setSaveConsumer(newValue -> configurations.set(booleanValue, newValue))
                .build());
            return;
        }
        if (value instanceof IntValue intValue)
        {
            category.addEntry(entryBuilder.startIntField(name, intValue.get())
                .setDefaultValue((Integer) spec.getDefaultValue())
                .setMin(spec.getMin() == null ? Integer.MIN_VALUE : spec.getMin().intValue())
                .setMax(spec.getMax() == null ? Integer.MAX_VALUE : spec.getMax().intValue())
                .setTooltip(tooltip)
                .setSaveConsumer(newValue -> configurations.set(intValue, newValue))
                .build());
            return;
        }
        if (value instanceof LongValue longValue)
        {
            category.addEntry(entryBuilder.startLongField(name, longValue.get())
                .setDefaultValue((Long) spec.getDefaultValue())
                .setMin(spec.getMin() == null ? Long.MIN_VALUE : spec.getMin().longValue())
                .setMax(spec.getMax() == null ? Long.MAX_VALUE : spec.getMax().longValue())
                .setTooltip(tooltip)
                .setSaveConsumer(newValue -> configurations.set(longValue, newValue))
                .build());
            return;
        }
        if (value instanceof DoubleValue doubleValue)
        {
            category.addEntry(entryBuilder.startDoubleField(name, doubleValue.get())
                .setDefaultValue((Double) spec.getDefaultValue())
                .setMin(spec.getMin() == null ? -Double.MAX_VALUE : spec.getMin().doubleValue())
                .setMax(spec.getMax() == null ? Double.MAX_VALUE : spec.getMax().doubleValue())
                .setTooltip(tooltip)
                .setSaveConsumer(newValue -> configurations.set(doubleValue, newValue))
                .build());
            return;
        }
        if (value instanceof EnumValue<?> enumValue && spec.getEnumClass() != null)
        {
            category.addEntry(entryBuilder.startEnumSelector(name, (Class) spec.getEnumClass(), enumValue.get())
                .setDefaultValue((Enum<?>) spec.getDefaultValue())
                .setTooltip(tooltip)
                .setSaveConsumer(newValue -> configurations.set((ConfigValue) enumValue, newValue))
                .build());
            return;
        }
        if (value.get() instanceof List<?> list && isStringList(list, spec.getDefaultValue()))
        {
            category.addEntry(entryBuilder.startStrList(name, (List<String>) value.get())
                .setDefaultValue(() -> (List<String>) spec.getDefaultValue())
                .setTooltip(tooltip)
                .setSaveConsumer(newValue -> configurations.set((ConfigValue) value, newValue))
                .build());
            return;
        }

        category.addEntry(entryBuilder.startStrField(name, String.valueOf(value.get()))
            .setDefaultValue(String.valueOf(spec.getDefaultValue()))
            .setTooltip(tooltip)
            .build());
    }

    private static boolean isStringList(final List<?> list, final Object defaultValue)
    {
        if (list.isEmpty())
        {
            return defaultValue instanceof List<?> defaults && defaults.stream().allMatch(String.class::isInstance);
        }
        return list.stream().allMatch(String.class::isInstance);
    }
}
