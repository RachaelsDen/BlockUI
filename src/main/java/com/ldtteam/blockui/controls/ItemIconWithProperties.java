package com.ldtteam.blockui.controls;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.PaneParams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Useful for overriding things like clock/compass textures. In xml defined through {@value #PARAM_PROPERTIES} key using nbt:
 * {<item registry key>:{<property name>:<float value>, ...}, ...}.
 * <p>
 * Special keys: {@value #NBT_CURRENT_ITEM} - refers to xml item (resolved during parsing not dynamic),
 * {@value #NBT_GENERIC_KEY} - generic properties
 * 
 * Deferred note: 26.2 item-model/property override compatibility is not restored in this batch.
 */
public class ItemIconWithProperties extends ItemIcon
{
    private static final String NBT_GENERIC_KEY = "_generic";
    private static final String NBT_CURRENT_ITEM = "_item";

    public static final String PARAM_PROPERTIES = "properties";

    protected final Map<Identifier, Float> genericPropertyOverrides = new HashMap<>();
    protected final Map<Item, Map<Identifier, Float>> itemPropertyOverrides = new HashMap<>();
    private Map<Identifier, Float> currentItemOverrides = Collections.emptyMap();

    public ItemIconWithProperties()
    {
        super();
    }

    public ItemIconWithProperties(final PaneParams paneParams)
    {
        super(paneParams);

        final String data = paneParams.getString(PARAM_PROPERTIES);
        if (data != null && itemStack != null)
        {
            final CompoundTag tag;
            try
            {
                tag = TagParser.parseCompoundFully(data);
            }
            catch (CommandSyntaxException e)
            {
                throw new RuntimeException(data, null);
            }
            tag.keySet().forEach(itemKey -> {
                if (tag.contains(itemKey))
                {
                    tag.getCompound(itemKey).ifPresent(child -> {
                        final var itemOverrides = NBT_GENERIC_KEY.equals(itemKey) ? genericPropertyOverrides :
                            itemPropertyOverrides.computeIfAbsent(NBT_CURRENT_ITEM.equals(itemKey) ? itemStack.getItem() :
                                BuiltInRegistries.ITEM.getValue(Identifier.parse(itemKey)), i -> new HashMap<>());

                        child.keySet().forEach(key -> {
                            if (child.contains(key))
                            {
                                child.getFloat(key).ifPresent(value -> itemOverrides.put(Identifier.parse(key), value));
                            }
                        });
                    });
                }
            });

            onItemUpdate();
        }
    }

    /**
     * Short call for adding itemProperty to current item
     */
    public void addPropertyForCurrentItem(final Identifier propertyKey, final float value)
    {
        itemPropertyOverrides
            .computeIfAbsent(Objects.requireNonNull(itemStack, "Call #setItem before this method").getItem(), item -> new HashMap<>())
            .put(propertyKey, value);
    }

    /**
     * @return modifiable all item-based overrides
     */
    public Map<Item, Map<Identifier, Float>> getItemPropertyOverrides()
    {
        return itemPropertyOverrides;
    }

    /**
     * @return modifiable generic overrides
     */
    public Map<Identifier, Float> getGenericPropertyOverrides()
    {
        return genericPropertyOverrides;
    }

    @Override
    public void drawSelf(final BOGuiGraphics target, final double mx, final double my)
    {
        if (isDataEmpty())
        {
            return;
        }
        super.drawSelf(target, mx, my);
    }

    @Override
    protected void onItemUpdate()
    {
        super.onItemUpdate();
        if (itemPropertyOverrides != null) // ctor race condition
        {
            currentItemOverrides = itemPropertyOverrides.getOrDefault(itemStack.getItem(), Collections.emptyMap());
        }
    }
}
