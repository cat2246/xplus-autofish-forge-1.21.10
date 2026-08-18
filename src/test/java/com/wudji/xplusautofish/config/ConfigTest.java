package com.wudji.xplusautofish.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigTest {
    @Test
    void defaultsMatchUpstream137() {
        Config c = new Config();
        assertAll(
                () -> assertTrue(c.isAutofishEnabled()),
                () -> assertFalse(c.isMultiRod()),
                () -> assertTrue(c.isOpenWaterDetectEnabled()),
                () -> assertEquals(1500L, c.getRecastDelay()),
                () -> assertEquals(50L, c.getRandomDelay()),
                () -> assertEquals(1L, c.getReelInDelay()),
                () -> assertEquals(30.0f, c.getTurnAngle()),
                () -> assertEquals(500, c.getTurnDuration())
        );
    }

    @Test
    void constraintsClampEveryNativeScreenRange() {
        Config c = new Config();
        c.setRecastDelay(50);
        c.setRandomDelay(900);
        c.setReelInDelay(-1);
        c.setTurnDuration(99_999);
        c.setTurnAngle(Float.NaN);
        assertTrue(c.enforceConstraints());
        assertAll(
                () -> assertEquals(500L, c.getRecastDelay()),
                () -> assertEquals(75L, c.getRandomDelay()),
                () -> assertEquals(1L, c.getReelInDelay()),
                () -> assertEquals(5000, c.getTurnDuration()),
                () -> assertEquals(30.0f, c.getTurnAngle())
        );
    }

    @Test
    void constraintsClampUpperAndLowerBoundsAndNormalizeRegex() {
        Config c = new Config();
        c.setRecastDelay(5_001);
        c.setRandomDelay(-1);
        c.setReelInDelay(2_001);
        c.setTurnDuration(99);
        c.setTurnAngle(Float.POSITIVE_INFINITY);
        c.setClearLagRegex(null);

        assertTrue(c.enforceConstraints());
        assertAll(
                () -> assertEquals(5_000L, c.getRecastDelay()),
                () -> assertEquals(0L, c.getRandomDelay()),
                () -> assertEquals(2_000L, c.getReelInDelay()),
                () -> assertEquals(100, c.getTurnDuration()),
                () -> assertEquals(30.0f, c.getTurnAngle()),
                () -> assertEquals("", c.getClearLagRegex())
        );
    }

    @Test
    void enforceConstraintsReportsNoChangeForValidValues() {
        assertFalse(new Config().enforceConstraints());
    }

    @Test
    void copyAndCopyFromPreserveAllFourteenFields() {
        Config source = new Config();
        source.setAutofishEnabled(false);
        source.setMultiRod(true);
        source.setOpenWaterDetectEnabled(false);
        source.setNoBreak(true);
        source.setPersistentMode(true);
        source.setUseSoundDetection(true);
        source.setForceMPDetection(true);
        source.setAutoTurnView(true);
        source.setTurnAngle(42.5f);
        source.setTurnDuration(1_234);
        source.setRecastDelay(2_345);
        source.setRandomDelay(67);
        source.setReelInDelay(890);
        source.setClearLagRegex("custom");

        Config copy = source.copy();
        Config target = new Config();
        target.copyFrom(source);

        assertNotSame(source, copy);
        assertNotSame(source, target);
        assertAll(
                () -> assertEquals(source.isAutofishEnabled(), copy.isAutofishEnabled()),
                () -> assertEquals(source.isMultiRod(), copy.isMultiRod()),
                () -> assertEquals(source.isOpenWaterDetectEnabled(), copy.isOpenWaterDetectEnabled()),
                () -> assertEquals(source.isNoBreak(), copy.isNoBreak()),
                () -> assertEquals(source.isPersistentMode(), copy.isPersistentMode()),
                () -> assertEquals(source.isUseSoundDetection(), copy.isUseSoundDetection()),
                () -> assertEquals(source.isForceMPDetection(), copy.isForceMPDetection()),
                () -> assertEquals(source.isAutoTurnView(), copy.isAutoTurnView()),
                () -> assertEquals(source.getTurnAngle(), copy.getTurnAngle()),
                () -> assertEquals(source.getTurnDuration(), copy.getTurnDuration()),
                () -> assertEquals(source.getRecastDelay(), copy.getRecastDelay()),
                () -> assertEquals(source.getRandomPercent(), copy.getRandomPercent()),
                () -> assertEquals(source.getReelInDelay(), copy.getReelInDelay()),
                () -> assertEquals(source.getClearLagRegex(), copy.getClearLagRegex()),
                () -> assertEquals(source.isAutofishEnabled(), target.isAutofishEnabled()),
                () -> assertEquals(source.isMultiRod(), target.isMultiRod()),
                () -> assertEquals(source.isOpenWaterDetectEnabled(), target.isOpenWaterDetectEnabled()),
                () -> assertEquals(source.isNoBreak(), target.isNoBreak()),
                () -> assertEquals(source.isPersistentMode(), target.isPersistentMode()),
                () -> assertEquals(source.isUseSoundDetection(), target.isUseSoundDetection()),
                () -> assertEquals(source.isForceMPDetection(), target.isForceMPDetection()),
                () -> assertEquals(source.isAutoTurnView(), target.isAutoTurnView()),
                () -> assertEquals(source.getTurnAngle(), target.getTurnAngle()),
                () -> assertEquals(source.getTurnDuration(), target.getTurnDuration()),
                () -> assertEquals(source.getRecastDelay(), target.getRecastDelay()),
                () -> assertEquals(source.getRandomPercent(), target.getRandomPercent()),
                () -> assertEquals(source.getReelInDelay(), target.getReelInDelay()),
                () -> assertEquals(source.getClearLagRegex(), target.getClearLagRegex())
        );
    }
}
