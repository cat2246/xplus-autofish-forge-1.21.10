package com.wudji.xplusautofish.gui;

import com.wudji.xplusautofish.config.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigDraftTest {
    @Test
    void draftDoesNotMutateLiveConfigUntilApplied() {
        Config live = new Config();
        ConfigDraft draft = new ConfigDraft(live);

        draft.values().setAutofishEnabled(false);

        assertTrue(live.isAutofishEnabled());
        draft.applyTo(live);
        assertFalse(live.isAutofishEnabled());
    }

    @Test
    void resetRestoresEveryUpstreamDefault() {
        Config live = new Config();
        live.setRecastDelay(4000);
        ConfigDraft draft = new ConfigDraft(live);

        draft.reset();

        assertEquals(1500L, draft.values().getRecastDelay());
        assertFalse(draft.differsFrom(new Config()));
    }

    @Test
    void valuesAreAnIndependentEditableCopy() {
        Config live = new Config();
        ConfigDraft draft = new ConfigDraft(live);

        assertNotSame(live, draft.values());
        assertSame(draft.values(), draft.values());
        draft.values().setClearLagRegex("custom");

        assertEquals("\\[ClearLag\\] Removed [0-9]+ Entities!", live.getClearLagRegex());
    }

    @Test
    void applyToEnforcesConstraintsBeforeCopyingValues() {
        Config live = new Config();
        ConfigDraft draft = new ConfigDraft(live);
        draft.values().setRecastDelay(100);
        draft.values().setRandomDelay(100);
        draft.values().setReelInDelay(3000);
        draft.values().setTurnDuration(6000);
        draft.values().setTurnAngle(Float.NaN);
        draft.values().setClearLagRegex(null);

        draft.applyTo(live);

        assertEquals(500L, live.getRecastDelay());
        assertEquals(75L, live.getRandomPercent());
        assertEquals(2000L, live.getReelInDelay());
        assertEquals(5000, live.getTurnDuration());
        assertEquals(30.0f, live.getTurnAngle());
        assertEquals("", live.getClearLagRegex());
    }

    @Test
    void differsFromComparesAllFourteenFields() {
        Config baseline = new Config();
        ConfigDraft draft = new ConfigDraft(baseline);

        draft.values().setAutofishEnabled(false);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setMultiRod(true);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setOpenWaterDetectEnabled(false);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setNoBreak(true);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setPersistentMode(true);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setUseSoundDetection(true);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setForceMPDetection(true);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setAutoTurnView(true);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setTurnAngle(42.5f);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setTurnDuration(1234);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setRecastDelay(2345);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setRandomPercent(67);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setReelInDelay(890);
        assertTrue(draft.differsFrom(baseline));
        draft.reset();
        draft.values().setClearLagRegex("custom");
        assertTrue(draft.differsFrom(baseline));
    }
}
