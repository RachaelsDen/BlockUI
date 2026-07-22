package com.ldtteam.blockui;

import com.ldtteam.blockui.util.cursor.Cursor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CursorSelectorTest
{
    @Test
    public void testDefaultState()
    {
        final CursorSelector selector = new CursorSelector();

        assertEquals(-1, selector.cursorMaxDepth());
        assertSame(Cursor.DEFAULT, selector.selectedCursor());
    }

    @Test
    public void testFirstCursorAlwaysWins()
    {
        final CursorSelector selector = new CursorSelector();
        final Cursor cursor = Cursor.named(() -> {}, "A");

        selector.consider(0, cursor);

        assertEquals(0, selector.cursorMaxDepth());
        assertSame(cursor, selector.selectedCursor());
    }

    @Test
    public void testEqualDepthReplacesCurrentCursor()
    {
        final CursorSelector selector = new CursorSelector();
        final Cursor first = Cursor.named(() -> {}, "A");
        final Cursor second = Cursor.named(() -> {}, "B");

        selector.consider(3, first);
        selector.consider(3, second);

        assertEquals(3, selector.cursorMaxDepth());
        assertSame(second, selector.selectedCursor());
    }

    @Test
    public void testDeeperDepthWinsAndShallowerDepthLoses()
    {
        final CursorSelector selector = new CursorSelector();
        final Cursor shallow = Cursor.named(() -> {}, "shallow");
        final Cursor deep = Cursor.named(() -> {}, "deep");
        final Cursor shallowerAgain = Cursor.named(() -> {}, "shallowerAgain");

        selector.consider(2, shallow);
        selector.consider(5, deep);
        selector.consider(4, shallowerAgain);

        assertEquals(5, selector.cursorMaxDepth());
        assertSame(deep, selector.selectedCursor());
    }

    @Test
    public void testNullCursorAtWinningDepthIsPreserved()
    {
        final CursorSelector selector = new CursorSelector();
        final Cursor earlier = Cursor.named(() -> {}, "earlier");

        selector.consider(2, earlier);
        selector.consider(2, null);

        assertEquals(2, selector.cursorMaxDepth());
        assertNull(selector.selectedCursor());
    }
}
