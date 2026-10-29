package com.example.wordcounter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link TextMetric}.
 */
public class TextMetricTest {

    @Test
    public void fromPosition_zero_returnsSentences() {
        assertEquals(TextMetric.SENTENCES, TextMetric.fromPosition(0));
    }

    @Test
    public void fromPosition_one_returnsWords() {
        assertEquals(TextMetric.WORDS, TextMetric.fromPosition(1));
    }

    @Test
    public void fromPosition_two_returnsChars() {
        assertEquals(TextMetric.CHARS, TextMetric.fromPosition(2));
    }

    @Test
    public void fromPosition_three_returnsNumbers() {
        assertEquals(TextMetric.NUMBERS, TextMetric.fromPosition(3));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void fromPosition_negativePosition_throwsException() {
        TextMetric.fromPosition(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void fromPosition_positionAfterLastItem_throwsException() {
        TextMetric.fromPosition(TextMetric.values().length);
    }

    @Test
    public void values_orderMatchesSpinnerItems() {
        // Spinner items in strings.xml: Sentences, Words, Chars, Numbers
        TextMetric[] expectedOrder = {
                TextMetric.SENTENCES, TextMetric.WORDS, TextMetric.CHARS, TextMetric.NUMBERS
        };
        assertArrayEquals(expectedOrder, TextMetric.values());
    }

    @Test
    public void valueOf_metricName_returnsMetric() {
        assertEquals(TextMetric.WORDS, TextMetric.valueOf("WORDS"));
    }
}
