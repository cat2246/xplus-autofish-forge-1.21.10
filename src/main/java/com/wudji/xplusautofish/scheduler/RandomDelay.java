package com.wudji.xplusautofish.scheduler;

import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Applies the upstream AutoFish random recast algorithm.
 *
 * <p>The first random draw chooses the direction and the second draw chooses
 * the magnitude. Keeping those draws separate preserves the upstream timing
 * distribution while allowing deterministic tests.</p>
 */
public final class RandomDelay {
    private RandomDelay() {
    }

    public static long compute(long base, long percent, DoubleSupplier random) {
        Objects.requireNonNull(random, "random");
        boolean decrease = random.getAsDouble() >= 0.5;
        double magnitude = random.getAsDouble();
        double adjustment = magnitude * percent * 0.01;
        return (long) (base * (decrease ? 1 - adjustment : 1 + adjustment));
    }
}
