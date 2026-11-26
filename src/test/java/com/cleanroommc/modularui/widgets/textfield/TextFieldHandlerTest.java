package com.cleanroommc.modularui.widgets.textfield;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextFieldHandlerTest {

    private static TextFieldHandler handler(String... lines) {
        TextFieldHandler handler = new TextFieldHandler(null) {

            @Override
            public void onChanged() {
                // The owning widget is not needed for these tests.
            }
        };
        handler.setRenderer(null);
        handler.setScrollArea(null);
        handler.getText()
            .clear();
        for (String line : lines) handler.getText()
            .add(line);
        return handler;
    }

    @Test
    void deleteTwoLineSelectionRemovesSecondLine() {
        TextFieldHandler handler = handler("abcdef", "ghijkl");
        handler.setCursor(0, 2, true, false);
        handler.setMainCursor(1, 2, true);

        handler.delete(false, false, false);

        assertEquals(Collections.singletonList("abijkl"), handler.getText());
    }

    @Test
    void deleteThreeLineSelectionRemovesMiddleLines() {
        TextFieldHandler handler = handler("abcd", "efgh", "ijkl");
        handler.setCursor(0, 3, true, false);
        handler.setMainCursor(2, 1, true);

        handler.delete(false, false, false);

        assertEquals(Collections.singletonList("abcjkl"), handler.getText());
    }

    @Test
    void deleteSingleLineSelectionJoinsRemainder() {
        TextFieldHandler handler = handler("abcd");
        handler.setCursor(0, 1, true, false);
        handler.setMainCursor(0, 3, true);

        handler.delete(false, false, false);

        assertEquals(Collections.singletonList("ad"), handler.getText());
    }
}
