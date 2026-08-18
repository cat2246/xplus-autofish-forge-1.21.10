package com.wudji.xplusautofish.scheduler;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RandomDelayTest {
    @Test
    void deterministicDrawsReachConfiguredLowerAndUpperBounds() {
        assertEquals(750L, RandomDelay.compute(1000, 25, draws(1.0, 1.0)));
        assertEquals(1250L, RandomDelay.compute(1000, 25, draws(0.0, 1.0)));
    }

    @Test
    void deterministicDrawsPreserveRepresentativeMagnitude() {
        assertEquals(875L, RandomDelay.compute(1000, 25, draws(1.0, 0.5)));
        assertEquals(1125L, RandomDelay.compute(1000, 25, draws(0.0, 0.5)));
    }

    @Test
    void randomDelayConsumesDirectionThenMagnitudeDraws() {
        AtomicInteger calls = new AtomicInteger();
        assertEquals(1125L, RandomDelay.compute(1000, 25, () -> {
            return calls.getAndIncrement() == 0 ? 0.0 : 0.5;
        }));
        assertEquals(2, calls.get());
    }

    private static java.util.function.DoubleSupplier draws(double... values) {
        AtomicInteger index = new AtomicInteger();
        return () -> values[index.getAndIncrement()];
    }
}
