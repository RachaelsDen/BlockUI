package com.ldtteam.common.config;

import com.ldtteam.common.config.AbstractConfiguration.Builder;
import com.ldtteam.common.config.AbstractConfiguration.ConfigValue;
import com.ldtteam.common.config.AbstractConfiguration.ConfigWatcher;
import com.ldtteam.common.config.AbstractConfiguration.ValueSpec;
import com.ldtteam.common.platform.EnvUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Mod root configuration.
 */
public class Configurations<CLIENT extends AbstractConfiguration,
    SERVER extends AbstractConfiguration,
    COMMON extends AbstractConfiguration>
{
    /**
     * Loaded clientside, not synced
     */
    private final ModConfig client;
    private final CLIENT clientConfig;

    /**
     * Loaded serverside (per world), synced on connection
     */
    private final ModConfig server;
    private final SERVER serverConfig;

    /**
     * Loaded both sides, not synced
     */
    private final ModConfig common;
    private final COMMON commonConfig;

    private final AbstractConfiguration[] activeConfigs;
    private final List<ConfigBackend> activeBackends = new ArrayList<>();
    private final String modId;

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configurations(final ModContainer modContainer,
        final ConfigEventSink modBus,
        final Function<Builder, CLIENT> clientFactory,
        final Function<Builder, SERVER> serverFactory,
        final Function<Builder, COMMON> commonFactory)
    {
        modId = modContainer == null ? "blockui" : modContainer.getModId();

        final List<AbstractConfiguration> configs = new ArrayList<>();

        final SimplePair<CLIENT, ModConfig> cli = createConfig(clientFactory, ModConfig.Type.CLIENT, configs);
        client = cli.getRight();
        clientConfig = cli.getLeft();

        final SimplePair<SERVER, ModConfig> ser = createConfig(serverFactory, ModConfig.Type.SERVER, configs);
        server = ser.getRight();
        serverConfig = ser.getLeft();

        final SimplePair<COMMON, ModConfig> com = createConfig(commonFactory, ModConfig.Type.COMMON, configs);
        common = com.getRight();
        commonConfig = com.getLeft();

        activeConfigs = configs.toArray(AbstractConfiguration[]::new);
        activeConfigs(activeConfigs).forEach(config -> config.watchers.forEach(ConfigWatcher::cacheLastValue));

        modBus.addListener(ModConfig.class, this::reload);

        if (EnvUtil.isClient())
        {
            ClientConfigHelper.register(this);
            ClientConfigHelper.registerClient(modContainer);
        }
    }

    private <T extends AbstractConfiguration> SimplePair<T, ModConfig> createConfig(final Function<Builder, T> factory,
        final ModConfig.Type type,
        final List<AbstractConfiguration> configs)
    {
        if (factory == null || (type == ModConfig.Type.CLIENT && !EnvUtil.isClient()))
        {
            return new SimplePair<>(null, null);
        }

        final Builder builder = new Builder();
        final T config = factory.apply(builder);
        final ConfigBackend backend = new ConfigBackend(modId, type, builder.values());
        config.bind(backend);
        backend.load();
        backend.save();
        activeBackends.add(backend);
        configs.add(config);

        return new SimplePair<>(config, new ModConfig(type, backend));
    }

    public CLIENT getClient()
    {
        return clientConfig;
    }

    public SERVER getServer()
    {
        return serverConfig;
    }

    public COMMON getCommon()
    {
        return commonConfig;
    }

    List<AbstractConfiguration> activeConfigs()
    {
        return activeConfigs(activeConfigs);
    }

    String getModId()
    {
        return modId;
    }

    void saveAll()
    {
        activeBackends.forEach(ConfigBackend::save);
    }

    void reloadAll()
    {
        activeBackends.forEach(ConfigBackend::reloadIfChanged);
        activeConfigs(activeConfigs).forEach(config -> config.watchers.forEach(ConfigWatcher::compareAndFireChangeEvent));
    }

    /**
     * Setter wrapper so watchers are fine. This should be called from any code that manually changes ConfigValues using set functions.
     * (Mostly done by settings UIs)
     */
    public <T> void set(final ConfigValue<T> configValue, final T value)
    {
        configValue.set(value);
        configValue.save();
        onConfigValueEdit(configValue);
    }

    /**
     * This should be called from any code that manually changes ConfigValues using set functions. (Mostly done by settings UIs)
     *
     * @param configValue which config value was changed
     */
    public void onConfigValueEdit(final ConfigValue<?> configValue)
    {
        for (final AbstractConfiguration cfg : activeConfigs)
        {
            for (final ConfigWatcher<?> configWatcher : cfg.watchers)
            {
                if (configWatcher.isSameForgeConfig(configValue))
                {
                    configWatcher.compareAndFireChangeEvent();
                }
            }
        }
    }

    /**
     * @param  value config value from this mod
     * @return       value spec, crashes in dev if not found
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public Optional<ValueSpec> getSpecFromValue(final ConfigValue<?> value)
    {
        return Optional.of(value.getSpec());
    }

    private void reload(final ModConfig ignored)
    {
        reloadAll();
    }

    private static List<AbstractConfiguration> activeConfigs(final AbstractConfiguration[] configs)
    {
        final List<AbstractConfiguration> active = new ArrayList<>(configs.length);
        for (final AbstractConfiguration config : configs)
        {
            if (config != null)
            {
                active.add(config);
            }
        }
        return active;
    }
}
