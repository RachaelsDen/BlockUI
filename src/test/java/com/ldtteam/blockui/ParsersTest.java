package com.ldtteam.blockui;

import net.minecraft.resources.Identifier;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ParsersTest
{
    @Test
    public void testBooleanParser()
    {
        assertTrue(Parsers.BOOLEAN.apply(null));
        assertTrue(Parsers.BOOLEAN.apply("true"));
        assertEquals(Boolean.FALSE, Parsers.BOOLEAN.apply("false"));
        assertEquals(Boolean.FALSE, Parsers.BOOLEAN.apply("disabled"));
        assertEquals(Boolean.FALSE, Parsers.BOOLEAN.apply(""));
    }

    @Test
    public void testResourceParser()
    {
        assertEquals(Identifier.withDefaultNamespace("stone"), Parsers.RESOURCE.apply("stone"));
        assertEquals(Identifier.fromNamespaceAndPath("blockui", "gui/test.xml"), Parsers.RESOURCE.apply("blockui:gui/test.xml"));
    }

    @Test
    public void testColorParserSupportsHexNameAndUnsignedInt()
    {
        assertEquals(Integer.valueOf(0x112233), Parsers.COLOR.apply("#112233"));
        assertEquals(Integer.valueOf(0xFF0000FF), Parsers.COLOR.apply("blue"));
        assertEquals(Integer.valueOf(123456789), Parsers.COLOR.apply("123456789"));
    }

    @Test
    public void testColorParseFallsBackToDefault()
    {
        assertEquals(0x12345678, Color.parse("not-a-real-color", 0x12345678));
        assertEquals(Integer.valueOf(0xFFFF0000), Color.getByName("RED"));
        assertNull(Color.getByName("not-a-real-color"));
    }

    @Test
    public void testScaledSingleSupportsPercentClampPixelsAndInvalid()
    {
        assertEquals(Integer.valueOf(50), Parsers.SCALED(200).apply("25%"));
        assertEquals(Integer.valueOf(200), Parsers.SCALED(200).apply("150%"));
        assertEquals(Integer.valueOf(15), Parsers.SCALED(200).apply("15px"));
        assertNull(Parsers.SCALED(200).apply("bogus"));
    }

    @Test
    public void testScaledMultipleRepeatsPairs()
    {
        final List<Integer> values = Parsers.SCALED(100, 200, 300, 400).apply("25% 10");
        assertEquals(Arrays.asList(25, 10, 75, 10), values);
    }

    @Test
    public void testEnumParser()
    {
        assertEquals(Alignment.TOP_LEFT, Parsers.ENUM(Alignment.class).apply("TOP_LEFT"));
        assertNull(Parsers.ENUM(Alignment.class).apply("NOPE"));
    }

    @Test
    public void testShorthandRepeatsTrailingPairPattern()
    {
        assertEquals(Arrays.asList(2, 8, 2, 8), Parsers.shorthand(Integer::parseInt, 4).apply("2 8"));
        assertEquals(Arrays.asList(1, 2, 3, 2), Parsers.shorthand(Integer::parseInt, 4).apply("1 2 3"));
    }
}
