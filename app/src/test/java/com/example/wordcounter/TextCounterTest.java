package com.example.wordcounter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Unit tests for {@link TextCounter}.
 * Every public method and every branch of the class is covered (100% line and branch coverage).
 * Test names follow the pattern: methodName_condition_expectedResult.
 */
public class TextCounterTest {

    private static final String SAMPLE_TEXT = "Hello world. I am 21 years old! Pi is 3.14,ok?";

    // ------------------------------------------------------------------
    // count(text, metric) - dispatching to the right counting method
    // ------------------------------------------------------------------

    @Test
    public void count_sentencesMetric_returnsSentenceCount() {
        assertEquals(3, TextCounter.count(SAMPLE_TEXT, TextMetric.SENTENCES));
    }

    @Test
    public void count_wordsMetric_returnsWordCount() {
        assertEquals(12, TextCounter.count(SAMPLE_TEXT, TextMetric.WORDS));
    }

    @Test
    public void count_charsMetric_returnsCharCount() {
        assertEquals(SAMPLE_TEXT.length(), TextCounter.count(SAMPLE_TEXT, TextMetric.CHARS));
    }

    @Test
    public void count_numbersMetric_returnsNumberCount() {
        assertEquals(2, TextCounter.count(SAMPLE_TEXT, TextMetric.NUMBERS));
    }

    @Test
    public void count_everyMetric_matchesDedicatedMethod() {
        String inputText = "Test 1. Test 2! Done";
        for (TextMetric metric : TextMetric.values()) {
            int expectedCount;
            switch (metric) {
                case SENTENCES:
                    expectedCount = TextCounter.countSentences(inputText);
                    break;
                case WORDS:
                    expectedCount = TextCounter.countWords(inputText);
                    break;
                case CHARS:
                    expectedCount = TextCounter.countChars(inputText);
                    break;
                default:
                    expectedCount = TextCounter.countNumbers(inputText);
                    break;
            }
            assertEquals("Metric " + metric, expectedCount, TextCounter.count(inputText, metric));
        }
    }

    @Test(expected = NullPointerException.class)
    public void count_nullMetric_throwsNullPointerException() {
        TextCounter.count(SAMPLE_TEXT, null);
    }

    // ------------------------------------------------------------------
    // countSentences
    // ------------------------------------------------------------------

    @Test
    public void countSentences_nullText_returnsZero() {
        assertEquals(0, TextCounter.countSentences(null));
    }

    @Test
    public void countSentences_emptyText_returnsZero() {
        assertEquals(0, TextCounter.countSentences(""));
    }

    @Test
    public void countSentences_onlySpaces_returnsZero() {
        assertEquals(0, TextCounter.countSentences("   \n\t "));
    }

    @Test
    public void countSentences_textWithoutPunctuation_returnsOne() {
        assertEquals(1, TextCounter.countSentences("just some words"));
    }

    @Test
    public void countSentences_differentEndings_countsEachSentence() {
        assertEquals(3, TextCounter.countSentences("First. Second! Third?"));
    }

    @Test
    public void countSentences_repeatedPunctuation_countsAsOneEnding() {
        assertEquals(2, TextCounter.countSentences("Wow!!! Really?!"));
    }

    @Test
    public void countSentences_decimalNumber_doesNotSplitSentence() {
        assertEquals(1, TextCounter.countSentences("Pi equals 3.14 approximately."));
    }

    @Test
    public void countSentences_lastSentenceWithoutDot_isCounted() {
        assertEquals(2, TextCounter.countSentences("Hello there. How are you"));
    }

    @Test
    public void countSentences_punctuationOnlyFragments_areIgnored() {
        // "! Hello. ! Bye." -> fragments "", " Hello", " ", " Bye" -> only 2 are real sentences
        assertEquals(2, TextCounter.countSentences("! Hello. ! Bye."));
    }

    @Test
    public void countSentences_multiLineText_countsAllLines() {
        assertEquals(2, TextCounter.countSentences("Line one.\nLine two."));
    }

    // ------------------------------------------------------------------
    // countWords
    // ------------------------------------------------------------------

    @Test
    public void countWords_nullText_returnsZero() {
        assertEquals(0, TextCounter.countWords(null));
    }

    @Test
    public void countWords_emptyText_returnsZero() {
        assertEquals(0, TextCounter.countWords(""));
    }

    @Test
    public void countWords_onlySpaces_returnsZero() {
        assertEquals(0, TextCounter.countWords("     "));
    }

    @Test
    public void countWords_singleWord_returnsOne() {
        assertEquals(1, TextCounter.countWords("hello"));
    }

    @Test
    public void countWords_separatedBySpacesCommasDots_countsAllWords() {
        assertEquals(3, TextCounter.countWords("one,two.three"));
        assertEquals(4, TextCounter.countWords("  one, two. three   four "));
    }

    @Test
    public void countWords_tabsAndNewLines_areSeparators() {
        assertEquals(3, TextCounter.countWords("one\ttwo\nthree"));
    }

    @Test
    public void countWords_leadingSeparator_isNotCountedAsWord() {
        // split(",hello") returns ["", "hello"]; the empty fragment must be skipped
        assertEquals(1, TextCounter.countWords(",hello"));
        assertEquals(2, TextCounter.countWords("..hello, world"));
    }

    @Test
    public void countWords_onlySeparators_returnsZero() {
        assertEquals(0, TextCounter.countWords(",.,."));
    }

    // ------------------------------------------------------------------
    // countChars
    // ------------------------------------------------------------------

    @Test
    public void countChars_nullText_returnsZero() {
        assertEquals(0, TextCounter.countChars(null));
    }

    @Test
    public void countChars_emptyText_returnsZero() {
        assertEquals(0, TextCounter.countChars(""));
    }

    @Test
    public void countChars_textWithSpaces_includesSpaces() {
        assertEquals(5, TextCounter.countChars("a b c"));
    }

    @Test
    public void countChars_onlySpaces_countsSpaces() {
        assertEquals(3, TextCounter.countChars("   "));
    }

    @Test
    public void countChars_punctuationAndDigits_areCounted() {
        assertEquals(6, TextCounter.countChars("A1, b!"));
    }

    // ------------------------------------------------------------------
    // countNumbers
    // ------------------------------------------------------------------

    @Test
    public void countNumbers_nullText_returnsZero() {
        assertEquals(0, TextCounter.countNumbers(null));
    }

    @Test
    public void countNumbers_blankText_returnsZero() {
        assertEquals(0, TextCounter.countNumbers("  "));
    }

    @Test
    public void countNumbers_textWithoutDigits_returnsZero() {
        assertEquals(0, TextCounter.countNumbers("no digits here"));
    }

    @Test
    public void countNumbers_integersAndDecimals_countsEachNumberOnce() {
        assertEquals(2, TextCounter.countNumbers(SAMPLE_TEXT));
        assertEquals(3, TextCounter.countNumbers("7 apples, 2026 year, 0.5 litre"));
    }

    @Test
    public void countNumbers_numbersInsideWords_areFound() {
        assertEquals(2, TextCounter.countNumbers("abc123def45"));
    }

    @Test
    public void countNumbers_trailingDot_isNotPartOfNumber() {
        assertEquals(1, TextCounter.countNumbers("Year 2026."));
    }

    // ------------------------------------------------------------------
    // isBlank
    // ------------------------------------------------------------------

    @Test
    public void isBlank_nullText_returnsTrue() {
        assertTrue(TextCounter.isBlank(null));
    }

    @Test
    public void isBlank_emptyText_returnsTrue() {
        assertTrue(TextCounter.isBlank(""));
    }

    @Test
    public void isBlank_whitespaceOnly_returnsTrue() {
        assertTrue(TextCounter.isBlank("  \t\n "));
    }

    @Test
    public void isBlank_textWithLetters_returnsFalse() {
        assertFalse(TextCounter.isBlank("  a  "));
    }

    // ------------------------------------------------------------------
    // Utility class contract
    // ------------------------------------------------------------------

    @Test
    public void constructor_isPrivate_andClassCanBeInstantiatedOnlyByReflection() throws Exception {
        Constructor<TextCounter> constructor = TextCounter.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        assertTrue(Modifier.isFinal(TextCounter.class.getModifiers()));

        constructor.setAccessible(true);
        assertNotNull(constructor.newInstance());
    }
}
