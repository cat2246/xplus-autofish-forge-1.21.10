package com.wudji.xplusautofish.config;

import com.wudji.xplusautofish.gui.ConfigDraft;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClearLagRegexTest {
    @Test
    void invalidDraftRegexKeepsTheLastValidValueWhenApplied() {
        Config live = new Config();
        ConfigDraft draft = new ConfigDraft(live);

        assertFalse(draft.trySetClearLagRegex("["));
        assertEquals(live.getClearLagRegex(), draft.values().getClearLagRegex());

        draft.values().setClearLagRegex("[");
        draft.applyTo(live);

        assertEquals("\\[ClearLag\\] Removed [0-9]+ Entities!", live.getClearLagRegex());
    }

    @Test
    void blankRegexIsValidButDisabled() {
        assertTrue(Config.isValidClearLagRegex("  \t"));
        assertNull(ClearLagPattern.compile("  \t"));
    }

    @Test
    void invalidPersistedRegexIsRejectedByProductionCompilationBoundary() {
        assertNull(ClearLagPattern.compile("["));
        Pattern pattern = ClearLagPattern.compile("ClearLag");
        assertNotNull(pattern);
        assertTrue(pattern.matcher("ClearLag").find());
    }
}
