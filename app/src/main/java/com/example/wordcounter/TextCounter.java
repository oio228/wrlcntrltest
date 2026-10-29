package com.example.wordcounter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class that calculates text metrics: sentences, words, characters and numbers.
 * The counting logic is kept outside of MainActivity, as required by the task.
 */
public final class TextCounter {

    /** Words can be separated by spaces (any whitespace), commas or dots. */
    private static final Pattern WORD_SEPARATOR_PATTERN = Pattern.compile("[\\s,.]+");

    /**
     * A sentence ends with one or more of the characters . ! ? followed by
     * whitespace or the end of the text (so "3.14" is not split into two sentences).
     */
    private static final Pattern SENTENCE_END_PATTERN = Pattern.compile("[.!?]+(?=\\s|$)");

    /** Integer or decimal number, e.g. 7, 2026, 3.14 */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");

    private TextCounter() {
        // Utility class, no instances needed
    }

    /**
     * Counts the selected metric in the given text.
     *
     * @param text   text entered by the user
     * @param metric what should be counted (must not be null)
     * @return number of found elements
     */
    public static int count(String text, TextMetric metric) {
        switch (metric) {
            case SENTENCES:
                return countSentences(text);
            case WORDS:
                return countWords(text);
            case CHARS:
                return countChars(text);
            case NUMBERS:
            default:
                // TextMetric has exactly four values, so the only remaining one is NUMBERS.
                // (The former "throw IllegalArgumentException" branch was unreachable dead code.)
                return countNumbers(text);
        }
    }

    public static int countSentences(String text) {
        if (isBlank(text)) {
            return 0;
        }
        int sentenceCount = 0;
        for (String sentence : SENTENCE_END_PATTERN.split(text)) {
            if (!sentence.trim().isEmpty()) {
                sentenceCount++;
            }
        }
        return sentenceCount;
    }

    public static int countWords(String text) {
        if (isBlank(text)) {
            return 0;
        }
        int wordCount = 0;
        for (String word : WORD_SEPARATOR_PATTERN.split(text.trim())) {
            if (!word.isEmpty()) {
                wordCount++;
            }
        }
        return wordCount;
    }

    public static int countChars(String text) {
        if (text == null) {
            return 0;
        }
        return text.length();
    }

    public static int countNumbers(String text) {
        if (isBlank(text)) {
            return 0;
        }
        Matcher numberMatcher = NUMBER_PATTERN.matcher(text);
        int numberCount = 0;
        while (numberMatcher.find()) {
            numberCount++;
        }
        return numberCount;
    }

    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
