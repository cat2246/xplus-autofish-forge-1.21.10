package com.wudji.xplusautofish.scheduler;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionTest {
    @Test
    void oneShotCompletesOnlyAfterDeadline() {
        AtomicLong now = new AtomicLong(1000);
        AtomicInteger executions = new AtomicInteger();
        Action action = new Action(ActionType.RECAST, 500, executions::incrementAndGet, now::get);

        assertFalse(action.tryExecute());
        assertEquals(0, executions.get());
        now.set(1500);
        assertTrue(action.tryExecute());
        assertEquals(1, executions.get());
    }

    @Test
    void repeatingActionResetsAfterExecution() {
        AtomicLong now = new AtomicLong(1000);
        AtomicInteger executions = new AtomicInteger();
        Action action = new Action(ActionType.REPEATING_ACTION, 500, executions::incrementAndGet, now::get);

        now.set(1500);
        assertTrue(action.tryExecute());
        assertEquals(1, executions.get());
        assertFalse(action.tryExecute());
        now.set(1999);
        assertFalse(action.tryExecute());
        now.set(2000);
        assertTrue(action.tryExecute());
        assertEquals(2, executions.get());
    }

    @Test
    void resetTimerUsesInjectedClock() {
        AtomicLong now = new AtomicLong(1000);
        AtomicInteger executions = new AtomicInteger();
        Action action = new Action(ActionType.RECAST, 500, executions::incrementAndGet, now::get);

        now.set(1200);
        action.resetTimer();
        now.set(1699);
        assertFalse(action.tryExecute());
        now.set(1700);
        assertTrue(action.tryExecute());
        assertEquals(1, executions.get());
    }
}
