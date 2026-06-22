package com.ldtteam.common.config;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigDefaultsTest
{
    @Test
    public void booleanValue_defaultIsCorrect()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.BooleanValue val = builder.define("testBool", true);

        assertEquals(true, val.getDefault());
        assertEquals(true, val.get());
    }

    @Test
    public void booleanValue_setChangesValue()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.BooleanValue val = builder.define("testBool", true);

        val.set(false);
        assertEquals(false, val.get());
        assertEquals(true, val.getDefault());
    }

    @Test
    public void intValue_defaultIsCorrect()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.IntValue val = builder.defineInRange("testInt", 42, 0, 100);

        assertEquals(Integer.valueOf(42), val.getDefault());
        assertEquals(Integer.valueOf(42), val.get());
    }

    @Test
    public void intValue_setWithinRange()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.IntValue val = builder.defineInRange("testInt", 50, 0, 100);

        val.set(75);
        assertEquals(Integer.valueOf(75), val.get());
    }

    @Test
    public void intValue_clampedToMin()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.IntValue val = builder.defineInRange("testInt", 50, 10, 100);

        val.set(-5);
        assertEquals(Integer.valueOf(10), val.get());
    }

    @Test
    public void intValue_clampedToMax()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.IntValue val = builder.defineInRange("testInt", 50, 10, 100);

        val.set(200);
        assertEquals(Integer.valueOf(100), val.get());
    }

    @Test
    public void longValue_defaultIsCorrect()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.LongValue val = builder.defineInRange("testLong", 1000L, 0L, 9999L);

        assertEquals(Long.valueOf(1000L), val.getDefault());
        assertEquals(Long.valueOf(1000L), val.get());
    }

    @Test
    public void doubleValue_defaultIsCorrect()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.DoubleValue val = builder.defineInRange("testDouble", 3.14, 0.0, 10.0);

        assertEquals(Double.valueOf(3.14), val.getDefault());
    }

    @Test
    public void stringValue_defaultIsCorrect()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.ConfigValue<String> val = builder.define("testStr", "default");

        assertEquals("default", val.getDefault());
        assertEquals("default", val.get());
    }

    @Test
    public void stringValue_setChangesValue()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.ConfigValue<String> val = builder.define("testStr", "default");

        val.set("custom");
        assertEquals("custom", val.get());
    }

    @Test
    public void valueSpec_hasCorrectPath()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.BooleanValue val = builder.define("myPath", false);

        assertEquals("myPath", val.getSpec().getPath());
    }

    @Test
    public void valueSpec_categoryPath_isDotted()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        builder.push("category1");
        builder.push("category2");
        final AbstractConfiguration.BooleanValue val = builder.define("leaf", true);
        builder.pop();
        builder.pop();

        assertEquals("category1.category2.leaf", val.getSpec().getPath());
    }

    @Test
    public void valueSpec_intHasMinMax()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.IntValue val = builder.defineInRange("ranged", 50, 10, 100);

        assertEquals(10, val.getSpec().getMin().intValue());
        assertEquals(100, val.getSpec().getMax().intValue());
    }

    @Test
    public void valueSpec_defaultValueMatchesDefined()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.IntValue val = builder.defineInRange("ranged", 42, 0, 99);

        assertEquals(42, val.getSpec().getDefaultValue());
    }

    @Test
    public void listValue_defaultIsCorrect()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.ConfigValue<List<? extends String>> val =
            builder.defineList("testList", List.of("a", "b"), obj -> obj instanceof String);

        final List<? extends String> result = val.get();
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test
    public void listValue_setReturnsCopy()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.ConfigValue<List<? extends String>> val =
            builder.defineList("testList", List.of("a", "b"), obj -> obj instanceof String);

        final List<? extends String> first = val.get();
        final List<? extends String> second = val.get();
        assertFalse("get() should return copies", first == second);
        assertEquals(first, second);
    }

    @Test
    public void restartType_none_isDefault()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.BooleanValue val = builder.define("testBool", true);

        assertEquals(AbstractConfiguration.RestartType.NONE, val.getSpec().getRestartType());
    }

    @Test
    public void restartType_worldRestart_setsCorrectly()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        builder.worldRestart();
        final AbstractConfiguration.BooleanValue val = builder.define("testBool", true);

        assertEquals(AbstractConfiguration.RestartType.WORLD, val.getSpec().getRestartType());
    }

    @Test
    public void restartType_gameRestart_setsCorrectly()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        builder.gameRestart();
        final AbstractConfiguration.BooleanValue val = builder.define("testBool", true);

        assertEquals(AbstractConfiguration.RestartType.GAME, val.getSpec().getRestartType());
    }

    @Test
    public void enumValue_defaultIsCorrect()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.EnumValue<TestEnum> val =
            builder.defineEnum("testEnum", TestEnum.SECOND);

        assertEquals(TestEnum.SECOND, val.getDefault());
        assertEquals(TestEnum.SECOND, val.get());
    }

    @Test
    public void enumValue_setChangesValue()
    {
        final AbstractConfiguration.Builder builder = new AbstractConfiguration.Builder();
        final AbstractConfiguration.EnumValue<TestEnum> val =
            builder.defineEnum("testEnum", TestEnum.SECOND);

        val.set(TestEnum.THIRD);
        assertEquals(TestEnum.THIRD, val.get());
    }

    enum TestEnum { FIRST, SECOND, THIRD }
}
