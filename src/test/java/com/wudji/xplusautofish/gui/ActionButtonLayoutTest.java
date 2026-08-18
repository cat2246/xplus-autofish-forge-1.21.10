package com.wudji.xplusautofish.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionButtonLayoutTest {
    @Test
    void actionButtonsAreCenteredAndDoNotOverlap() {
        int[] x = AutoFishConfigScreen.actionButtonX(400);

        assertEquals(55, x[0]);
        assertEquals(155, x[1]);
        assertEquals(255, x[2]);
        assertTrue(x[0] + AutoFishConfigScreen.ACTION_BUTTON_WIDTH <= x[1]);
        assertTrue(x[1] + AutoFishConfigScreen.ACTION_BUTTON_WIDTH <= x[2]);
    }
}
