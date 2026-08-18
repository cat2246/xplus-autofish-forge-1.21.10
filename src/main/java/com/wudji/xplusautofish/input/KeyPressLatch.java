package com.wudji.xplusautofish.input;

/** Detects the rising edge of a key's down state. */
public final class KeyPressLatch {
    private boolean previouslyDown;

    public boolean update(boolean down) {
        boolean pressed = down && !previouslyDown;
        previouslyDown = down;
        return pressed;
    }
}
