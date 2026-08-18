package com.wudji.xplusautofish.config;

import com.google.gson.annotations.Expose;

public class Config {
    @Expose
    boolean isAutofishEnabled = true;
    @Expose boolean multiRod = false;
    @Expose boolean isOpenWaterDetectEnabled = true;
    @Expose boolean noBreak = false;
    @Expose boolean persistentMode = false;
    @Expose boolean useSoundDetection = false;
    @Expose boolean forceMPDetection = false;
    @Expose boolean autoTurnView = false;
    @Expose float turnAngle = 30.0f;
    @Expose int turnDuration = 500;
    @Expose long recastDelay = 1500;
    @Expose long randomPercent = 50;
    @Expose long reelInDelay = 1;
    @Expose String clearLagRegex = "\\[ClearLag\\] Removed [0-9]+ Entities!";

    public boolean isAutofishEnabled() {
        return isAutofishEnabled;
    }
    public boolean isOpenWaterDetectEnabled() {
        return isOpenWaterDetectEnabled;
    }

    public boolean isMultiRod() {
        return multiRod;
    }

    public boolean isNoBreak() {
        return noBreak;
    }

    public boolean isPersistentMode() { return persistentMode; }

    public boolean isUseSoundDetection() {
        return useSoundDetection;
    }

    public boolean isForceMPDetection() { return forceMPDetection; }

    public boolean isAutoTurnView() {
        return autoTurnView;
    }

    public void setAutoTurnView(boolean autoTurnView) {
        this.autoTurnView = autoTurnView;
    }

    public float getTurnAngle() {
        return turnAngle;
    }

    public void setTurnAngle(float turnAngle) {
        this.turnAngle = turnAngle;
    }

    public int getTurnDuration() {
        return turnDuration;
    }

    public void setTurnDuration(int turnDuration) {
        this.turnDuration = turnDuration;
    }

    public long getRandomPercent() {
        return randomPercent;
    }

    public void setRandomPercent(long randomPercent) {
        this.randomPercent = randomPercent;
    }

    public long getRecastDelay() {
        return recastDelay;
    }

    public long getRandomDelay(){
        return randomPercent;
    }

    public long getReelInDelay() {
        return reelInDelay;
    }

    public String getClearLagRegex() {
        return clearLagRegex;
    }

    public void setAutofishEnabled(boolean autofishEnabled) { isAutofishEnabled = autofishEnabled; }

    public void setMultiRod(boolean multiRod) {
        this.multiRod = multiRod;
    }

    public void setNoBreak(boolean noBreak) {
        this.noBreak = noBreak;
    }

    public void setPersistentMode(boolean persistentMode) { this.persistentMode = persistentMode; }

    public void setUseSoundDetection(boolean useSoundDetection) {
        this.useSoundDetection = useSoundDetection;
    }

    public void setForceMPDetection(boolean forceMPDetection) { this.forceMPDetection = forceMPDetection; }

    public void setRecastDelay(long recastDelay) {
        this.recastDelay = recastDelay;
    }

    public void setRandomDelay(long randomPercent){
        this.randomPercent = randomPercent;
    }

    public void setReelInDelay(long reelInDelay) {
        this.reelInDelay = reelInDelay;
    }

    public void setClearLagRegex(String clearLagRegex) {
        this.clearLagRegex = clearLagRegex;
    }

    public static boolean isValidClearLagRegex(String regex) {
        return ClearLagPattern.isValid(regex);
    }

    public void setOpenWaterDetectEnabled(boolean openWaterDetectEnabled) {
        isOpenWaterDetectEnabled = openWaterDetectEnabled;
    }

    public Config copy() {
        Config copy = new Config();
        copy.copyFrom(this);
        return copy;
    }

    public void copyFrom(Config config) {
        isAutofishEnabled = config.isAutofishEnabled;
        multiRod = config.multiRod;
        isOpenWaterDetectEnabled = config.isOpenWaterDetectEnabled;
        noBreak = config.noBreak;
        persistentMode = config.persistentMode;
        useSoundDetection = config.useSoundDetection;
        forceMPDetection = config.forceMPDetection;
        autoTurnView = config.autoTurnView;
        turnAngle = config.turnAngle;
        turnDuration = config.turnDuration;
        recastDelay = config.recastDelay;
        randomPercent = config.randomPercent;
        reelInDelay = config.reelInDelay;
        clearLagRegex = config.clearLagRegex;
    }

    /**
     * @return true if anything was changed
     */
    public boolean enforceConstraints() {
        boolean changed = false;
        long constrainedRecastDelay = clamp(recastDelay, 500, 5000);
        if (recastDelay != constrainedRecastDelay) {
            recastDelay = constrainedRecastDelay;
            changed = true;
        }
        long constrainedRandomPercent = clamp(randomPercent, 0, 75);
        if (randomPercent != constrainedRandomPercent) {
            randomPercent = constrainedRandomPercent;
            changed = true;
        }
        long constrainedReelInDelay = clamp(reelInDelay, 1, 2000);
        if (reelInDelay != constrainedReelInDelay) {
            reelInDelay = constrainedReelInDelay;
            changed = true;
        }
        int constrainedTurnDuration = clamp(turnDuration, 100, 5000);
        if (turnDuration != constrainedTurnDuration) {
            turnDuration = constrainedTurnDuration;
            changed = true;
        }
        if (!Float.isFinite(turnAngle)) {
            turnAngle = 30.0f;
            changed = true;
        }
        if (clearLagRegex == null) {
            clearLagRegex = "";
            changed = true;
        } else if (!isValidClearLagRegex(clearLagRegex)) {
            clearLagRegex = "";
            changed = true;
        }
        return changed;
    }

    private static long clamp(long value, long minimum, long maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
