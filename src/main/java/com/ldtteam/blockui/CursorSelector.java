package com.ldtteam.blockui;

import com.ldtteam.blockui.util.cursor.Cursor;

/**
 * Keeps the highest-priority cursor seen during a draw pass.
 * The current rule is: deeper draw depth wins, and ties go to the later cursor.
 */
public class CursorSelector
{
    private int cursorMaxDepth = -1;
    private Cursor selectedCursor = Cursor.DEFAULT;

    public void consider(final int depth, final Cursor cursor)
    {
        if (depth >= cursorMaxDepth)
        {
            cursorMaxDepth = depth;
            selectedCursor = cursor;
        }
    }

    public Cursor selectedCursor()
    {
        return selectedCursor;
    }

    public int cursorMaxDepth()
    {
        return cursorMaxDepth;
    }
}
