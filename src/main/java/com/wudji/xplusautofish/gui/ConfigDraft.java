package com.wudji.xplusautofish.gui;

import com.wudji.xplusautofish.config.Config;

import java.util.Objects;

/** Editable settings copy used by the native configuration screen. */
public final class ConfigDraft {
    private Config editableValues;
    private String lastValidClearLagRegex;

    public ConfigDraft(Config source) {
        editableValues = source.copy();
        if (!Config.isValidClearLagRegex(editableValues.getClearLagRegex())) {
            editableValues.setClearLagRegex("");
        }
        lastValidClearLagRegex = editableValues.getClearLagRegex();
    }

    public Config values() {
        return editableValues;
    }

    public void reset() {
        editableValues.copyFrom(new Config());
        lastValidClearLagRegex = editableValues.getClearLagRegex();
    }

    /** Applies a UI edit only when the candidate is a valid regex. */
    public boolean trySetClearLagRegex(String candidate) {
        if (!Config.isValidClearLagRegex(candidate)) {
            return false;
        }
        editableValues.setClearLagRegex(candidate);
        lastValidClearLagRegex = candidate;
        return true;
    }

    public void applyTo(Config target) {
        if (!Config.isValidClearLagRegex(editableValues.getClearLagRegex())) {
            editableValues.setClearLagRegex(lastValidClearLagRegex);
        }
        editableValues.enforceConstraints();
        target.copyFrom(editableValues);
    }

    public boolean differsFrom(Config other) {
        return editableValues.isAutofishEnabled() != other.isAutofishEnabled()
                || editableValues.isMultiRod() != other.isMultiRod()
                || editableValues.isOpenWaterDetectEnabled() != other.isOpenWaterDetectEnabled()
                || editableValues.isNoBreak() != other.isNoBreak()
                || editableValues.isPersistentMode() != other.isPersistentMode()
                || editableValues.isUseSoundDetection() != other.isUseSoundDetection()
                || editableValues.isForceMPDetection() != other.isForceMPDetection()
                || editableValues.isAutoTurnView() != other.isAutoTurnView()
                || Float.compare(editableValues.getTurnAngle(), other.getTurnAngle()) != 0
                || editableValues.getTurnDuration() != other.getTurnDuration()
                || editableValues.getRecastDelay() != other.getRecastDelay()
                || editableValues.getRandomPercent() != other.getRandomPercent()
                || editableValues.getReelInDelay() != other.getReelInDelay()
                || !Objects.equals(editableValues.getClearLagRegex(), other.getClearLagRegex());
    }
}
