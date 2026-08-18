package com.wudji.xplusautofish.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyPressLatchTest {
    @Test
    void firesOnceUntilKeyIsReleased() {
        KeyPressLatch latch = new KeyPressLatch();
        assertFalse(latch.update(false));
        assertTrue(latch.update(true));
        assertFalse(latch.update(true));
        assertFalse(latch.update(false));
        assertTrue(latch.update(true));
    }
}
