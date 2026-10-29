package com.example.wordcounter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

/**
 * JVM unit tests for {@link MainActivity} (Robolectric).
 * They cover all methods and branches of the activity class:
 * onCreate, setUpMetricSpinner, onCountButtonClick (blank and non-blank input).
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class MainActivityTest {

    private static final String SAMPLE_TEXT = "Hello world. I am 21 years old! Pi is 3.14,ok?";

    private ActivityController<MainActivity> activityController;
    private MainActivity mainActivity;
    private EditText edtInputText;
    private Spinner spnMetric;
    private Button btnCount;
    private TextView txtResult;

    @Before
    public void setUp() {
        ShadowToast.reset();
        activityController = Robolectric.buildActivity(MainActivity.class).setup();
        mainActivity = activityController.get();

        edtInputText = mainActivity.findViewById(R.id.edtInputText);
        spnMetric = mainActivity.findViewById(R.id.spnMetric);
        btnCount = mainActivity.findViewById(R.id.btnCount);
        txtResult = mainActivity.findViewById(R.id.txtResult);
    }

    @After
    public void tearDown() {
        activityController.pause().stop().destroy();
    }

    // ------------------------------------------------------------------
    // onCreate / setUpMetricSpinner
    // ------------------------------------------------------------------

    @Test
    public void onCreate_allViewsAreCreated() {
        assertNotNull(edtInputText);
        assertNotNull(spnMetric);
        assertNotNull(btnCount);
        assertNotNull(txtResult);
        assertEquals("", txtResult.getText().toString());
    }

    @Test
    public void setUpMetricSpinner_containsAllMetricsInEnumOrder() {
        String[] expectedOptions = mainActivity.getResources().getStringArray(R.array.metric_options);
        SpinnerAdapter metricAdapter = spnMetric.getAdapter();

        assertEquals(TextMetric.values().length, metricAdapter.getCount());
        for (int position = 0; position < expectedOptions.length; position++) {
            assertEquals(expectedOptions[position], metricAdapter.getItem(position).toString());
        }
    }

    @Test
    public void setUpMetricSpinner_firstMetricIsSelectedByDefault() {
        assertEquals(0, spnMetric.getSelectedItemPosition());
        assertEquals(TextMetric.SENTENCES, TextMetric.fromPosition(spnMetric.getSelectedItemPosition()));
    }

    // ------------------------------------------------------------------
    // onCountButtonClick - valid text
    // ------------------------------------------------------------------

    @Test
    public void countButton_sentencesSelected_showsSentenceCount() {
        enterTextSelectMetricAndClickCount(SAMPLE_TEXT, TextMetric.SENTENCES);
        assertEquals("Sentences: 3", txtResult.getText().toString());
    }

    @Test
    public void countButton_wordsSelected_showsWordCount() {
        enterTextSelectMetricAndClickCount(SAMPLE_TEXT, TextMetric.WORDS);
        assertEquals("Words: 12", txtResult.getText().toString());
    }

    @Test
    public void countButton_charsSelected_showsCharCount() {
        enterTextSelectMetricAndClickCount(SAMPLE_TEXT, TextMetric.CHARS);
        assertEquals("Chars: " + SAMPLE_TEXT.length(), txtResult.getText().toString());
    }

    @Test
    public void countButton_numbersSelected_showsNumberCount() {
        enterTextSelectMetricAndClickCount(SAMPLE_TEXT, TextMetric.NUMBERS);
        assertEquals("Numbers: 2", txtResult.getText().toString());
    }

    @Test
    public void countButton_validText_doesNotShowToast() {
        enterTextSelectMetricAndClickCount("Some text", TextMetric.WORDS);
        assertNull(ShadowToast.getLatestToast());
    }

    // ------------------------------------------------------------------
    // onCountButtonClick - empty / blank text
    // ------------------------------------------------------------------

    @Test
    public void countButton_emptyText_showsToastAndEmptyResult() {
        enterTextSelectMetricAndClickCount("", TextMetric.WORDS);

        assertEquals(mainActivity.getString(R.string.toast_empty_text),
                ShadowToast.getTextOfLatestToast());
        assertEquals("", txtResult.getText().toString());
    }

    @Test
    public void countButton_onlySpaces_showsToastAndEmptyResult() {
        enterTextSelectMetricAndClickCount("    ", TextMetric.CHARS);

        assertEquals(mainActivity.getString(R.string.toast_empty_text),
                ShadowToast.getTextOfLatestToast());
        assertEquals("", txtResult.getText().toString());
    }

    @Test
    public void countButton_textClearedAfterCount_previousResultIsCleared() {
        enterTextSelectMetricAndClickCount("one two three", TextMetric.WORDS);
        assertEquals("Words: 3", txtResult.getText().toString());

        enterTextSelectMetricAndClickCount("", TextMetric.WORDS);
        assertEquals("", txtResult.getText().toString());
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private void enterTextSelectMetricAndClickCount(String inputText, TextMetric metric) {
        edtInputText.setText(inputText);
        spnMetric.setSelection(metric.ordinal());
        shadowOf(Looper.getMainLooper()).idle();

        btnCount.performClick();
        shadowOf(Looper.getMainLooper()).idle();
    }
}
