package com.cleanroommc.modularui.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathUtilsTest {

    @Test
    void testExplicitPercentageRelativeToMaximum() {
        assertEquals(1, MathUtils.percentOrSelf("1", 1, 256));
        assertEquals(128, MathUtils.percentOrSelf("0.5", 0.5, 256));
        assertEquals(128, MathUtils.percentOrSelf("50%", 0.5, 256));
        assertEquals(256, MathUtils.percentOrSelf("100%", 1, 256));
        assertEquals(512, MathUtils.percentOrSelf("200%", 2, 256));
    }
}
