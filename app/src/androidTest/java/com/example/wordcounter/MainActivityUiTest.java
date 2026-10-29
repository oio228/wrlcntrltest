package com.example.wordcounter;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.hasToString;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI / integration tests for {@link MainActivity}.
 * They run on a real device or emulator (Espresso + UiAutomator) and check
 * user interaction with the activity and device features:
 * soft keyboard, system clipboard, screen rotation, background/foreground switching.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityUiTest {

    private static final String SAMPLE_TEXT = "Hello world. I am 21 years old! Pi is 3.14,ok?";

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private Context targetContext;
    private UiDevice uiDevice;

    @Before
    public void setUp() throws RemoteException {
        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        uiDevice.setOrientationNatural();
    }

    @After
    public void tearDown() throws RemoteException {
        // Return the device to its normal state after the rotation test
        uiDevice.setOrientationNatural();
        uiDevice.unfreezeRotation();
    }

    // ------------------------------------------------------------------
    // Scenario 1: counting every metric through the GUI
    // ------------------------------------------------------------------

    @Test
    public void countSentences_sampleText_showsThreeSentences() {
        enterText(SAMPLE_TEXT);
        selectMetric(TextMetric.SENTENCES);
        clickCountButton();
        checkResult(TextMetric.SENTENCES, 3);
    }

    @Test
    public void countWords_sampleText_showsTwelveWords() {
        enterText(SAMPLE_TEXT);
        selectMetric(TextMetric.WORDS);
        clickCountButton();
        checkResult(TextMetric.WORDS, 12);
    }

    @Test
    public void countChars_sampleText_showsFortySixChars() {
        enterText(SAMPLE_TEXT);
        selectMetric(TextMetric.CHARS);
        clickCountButton();
        checkResult(TextMetric.CHARS, 46);
    }

    @Test
    public void countNumbers_sampleText_showsTwoNumbers() {
        enterText(SAMPLE_TEXT);
        selectMetric(TextMetric.NUMBERS);
        clickCountButton();
        checkResult(TextMetric.NUMBERS, 2);
    }

    @Test
    public void changeMetric_sameText_resultIsUpdated() {
        enterText("One two. Three four five.");

        selectMetric(TextMetric.WORDS);
        clickCountButton();
        checkResult(TextMetric.WORDS, 5);

        selectMetric(TextMetric.SENTENCES);
        clickCountButton();
        checkResult(TextMetric.SENTENCES, 2);
    }

    // ------------------------------------------------------------------
    // Scenario 2: spinner shows all metrics
    // ------------------------------------------------------------------

    @Test
    public void metricSpinner_opened_showsAllMetrics() {
        onView(withId(R.id.spnMetric)).perform(click());
        for (TextMetric metric : TextMetric.values()) {
            onData(hasToString(metricName(metric)))
                    .inRoot(isPlatformPopup())
                    .check(matches(isDisplayed()));
        }
        pressBack();
        onView(withId(R.id.spnMetric)).check(matches(withSpinnerText(metricName(TextMetric.SENTENCES))));
    }

    // ------------------------------------------------------------------
    // Scenario 3: empty / blank input is rejected
    // ------------------------------------------------------------------

    @Test
    public void countButton_emptyInput_resultStaysEmpty() {
        enterText("");
        clickCountButton();
        onView(withId(R.id.txtResult)).check(matches(withText("")));
    }

    @Test
    public void countButton_blankInputAfterValidCount_previousResultIsCleared() {
        enterText("one two three");
        selectMetric(TextMetric.WORDS);
        clickCountButton();
        checkResult(TextMetric.WORDS, 3);

        enterText("     ");
        clickCountButton();
        onView(withId(R.id.txtResult)).check(matches(withText("")));
    }

    // ------------------------------------------------------------------
    // Scenario 4: typing with the device soft keyboard
    // ------------------------------------------------------------------

    @Test
    public void typeTextWithKeyboard_countWords_showsWordCount() {
        onView(withId(R.id.edtInputText)).perform(typeText("Hello Android world"));
        closeSoftKeyboard();
        selectMetric(TextMetric.WORDS);
        clickCountButton();
        checkResult(TextMetric.WORDS, 3);
    }

    // ------------------------------------------------------------------
    // Scenario 5: pasting text from the system clipboard
    // ------------------------------------------------------------------

    @Test
    public void pasteFromClipboard_countNumbers_showsNumberCount() {
        String copiedText = "Order 15 costs 9.99 and ships in 3 days";
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ClipboardManager clipboardManager =
                    (ClipboardManager) targetContext.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("copied text", copiedText));
        });

        onView(withId(R.id.edtInputText)).perform(click(), pasteFromClipboard());
        closeSoftKeyboard();
        onView(withId(R.id.edtInputText)).check(matches(withText(copiedText)));

        selectMetric(TextMetric.NUMBERS);
        clickCountButton();
        checkResult(TextMetric.NUMBERS, 3);
    }

    // ------------------------------------------------------------------
    // Scenario 6: screen rotation (activity is re-created)
    // ------------------------------------------------------------------

    @Test
    public void rotateScreen_inputAndMetricAreKept_countStillWorks() throws RemoteException {
        enterText(SAMPLE_TEXT);
        selectMetric(TextMetric.NUMBERS);

        uiDevice.setOrientationLeft();
        uiDevice.waitForIdle();
        closeSoftKeyboard();

        onView(withId(R.id.edtInputText)).check(matches(withText(SAMPLE_TEXT)));
        onView(withId(R.id.spnMetric)).check(matches(withSpinnerText(metricName(TextMetric.NUMBERS))));

        clickCountButton();
        checkResult(TextMetric.NUMBERS, 2);
    }

    // ------------------------------------------------------------------
    // Scenario 7: app goes to background and returns
    // ------------------------------------------------------------------

    @Test
    public void appSentToBackgroundAndBack_resultIsKept() {
        enterText(SAMPLE_TEXT);
        selectMetric(TextMetric.SENTENCES);
        clickCountButton();
        checkResult(TextMetric.SENTENCES, 3);

        activityRule.getScenario().moveToState(Lifecycle.State.CREATED);
        activityRule.getScenario().moveToState(Lifecycle.State.RESUMED);

        checkResult(TextMetric.SENTENCES, 3);
        onView(withId(R.id.edtInputText)).check(matches(withText(SAMPLE_TEXT)));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void enterText(String inputText) {
        onView(withId(R.id.edtInputText)).perform(replaceText(inputText));
        closeSoftKeyboard();
    }

    private void selectMetric(TextMetric metric) {
        onView(withId(R.id.spnMetric)).perform(click());
        onData(hasToString(metricName(metric))).inRoot(isPlatformPopup()).perform(click());
        onView(withId(R.id.spnMetric)).check(matches(withSpinnerText(metricName(metric))));
    }

    private void clickCountButton() {
        onView(withId(R.id.btnCount)).perform(scrollTo(), click());
    }

    private void checkResult(TextMetric metric, int expectedCount) {
        String expectedResult = targetContext.getString(
                R.string.result_format, metricName(metric), expectedCount);
        onView(withId(R.id.txtResult)).check(matches(withText(expectedResult)));
    }

    private String metricName(TextMetric metric) {
        return targetContext.getResources().getStringArray(R.array.metric_options)[metric.ordinal()];
    }

    /** Pastes the system clipboard content into an EditText, like the "Paste" menu item. */
    private static ViewAction pasteFromClipboard() {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(EditText.class);
            }

            @Override
            public String getDescription() {
                return "paste text from the system clipboard";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ((EditText) view).onTextContextMenuItem(android.R.id.paste);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
