package com.example.wordcounter;

/**
 * Metrics that can be counted. The order must match the
 * "metric_options" string-array in strings.xml (Spinner items).
 */
public enum TextMetric {
    SENTENCES,
    WORDS,
    CHARS,
    NUMBERS;

    public static TextMetric fromPosition(int spinnerPosition) {
        return values()[spinnerPosition];
    }
}
