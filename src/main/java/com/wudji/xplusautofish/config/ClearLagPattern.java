package com.wudji.xplusautofish.config;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/** Shared validation boundary for the optional ClearLag chat pattern. */
public final class ClearLagPattern {
    private ClearLagPattern() {
    }

    /**
     * Compiles a configured pattern, returning {@code null} for the disabled
     * blank value or malformed legacy/manual configuration.
     */
    public static Pattern compile(String regex) {
        if (StringUtils.deleteWhitespace(regex == null ? "" : regex).isEmpty()) {
            return null;
        }
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException ignored) {
            return null;
        }
    }

    public static boolean isValid(String regex) {
        return regex == null || StringUtils.deleteWhitespace(regex).isEmpty()
                || compile(regex) != null;
    }
}
