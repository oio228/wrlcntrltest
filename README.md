# WordCounter — Lab 3 Android App Testing

The app counts sentences, words, characters and numbers in a text.
This project adds **unit tests** (JVM, JUnit 4 + Robolectric), **UI tests** (Espresso + UiAutomator)
and a **JaCoCo** code coverage report.

## Test structure

```
app/src/test/java/com/example/wordcounter/          <- Unit tests (run on the JVM, no device)
    TextCounterTest.java    40 tests - all counting logic
    TextMetricTest.java      8 tests - metric enum
    MainActivityTest.java   11 tests - activity via Robolectric
app/src/androidTest/java/com/example/wordcounter/   <- UI tests (emulator / device)
    MainActivityUiTest.java 12 tests, 7 scenarios
```

## How to run in Android Studio

1. **File -> Open** -> select the `WordCounter` folder and wait for Gradle Sync.
   JDK 17+ is required (the JDK bundled with Android Studio works).
2. **Unit tests:** right-click `app/src/test/java` -> **Run 'Tests in ...'**.
3. **Coverage:** right-click the same folder -> **Run 'Tests in ...' with Coverage**,
   or generate the JaCoCo report from the Android Studio terminal:
   ```
   gradlew createDebugUnitTestCoverageReport
   ```
   Report: `app/build/reports/coverage/test/debug/index.html`
4. **UI tests:** start an emulator (API 24-34) -> right-click `app/src/androidTest/java` -> **Run**,
   or run `gradlew connectedDebugAndroidTest`
   (report: `app/build/reports/androidTests/connected/debug/index.html`).
   It is recommended to turn off animations on the emulator first
   (Settings -> Developer options -> Window/Transition/Animator scale = Off).

## Unit tests (100% class and method coverage)

| Class | What is tested |
|---|---|
| `TextCounter` | `count()` for every metric and for `null`; `countSentences`, `countWords`, `countChars`, `countNumbers` with `null`, empty string, spaces, normal text and edge cases (`3.14` does not split a sentence, `!!!` is one sentence ending, commas and dots separate words, numbers inside words); `isBlank`; private constructor of the utility class |
| `TextMetric` | `fromPosition()` for all valid positions and invalid ones (`-1`, `4`); enum order matches the Spinner items |
| `MainActivity` | screen creation, Spinner items, counting for every metric, empty input -> Toast and empty result, previous result is cleared |

Every `if`/`switch` branch in `TextCounter`, `TextMetric` and `MainActivity` is executed by at least one test.

## UI / integration tests (7 scenarios, at least 4 required)

| # | Scenario | Tests |
|---|---|---|
| 1 | Counting every metric through the GUI (enter text -> select metric -> Count) | `countSentences_...`, `countWords_...`, `countChars_...`, `countNumbers_...`, `changeMetric_...` |
| 2 | Spinner shows all 4 metrics | `metricSpinner_opened_showsAllMetrics` |
| 3 | Empty / blank input is not counted, previous result is cleared | `countButton_emptyInput_...`, `countButton_blankInputAfterValidCount_...` |
| 4 | Typing with the device **soft keyboard** | `typeTextWithKeyboard_countWords_showsWordCount` |
| 5 | Pasting from the **system clipboard** | `pasteFromClipboard_countNumbers_showsNumberCount` |
| 6 | **Screen rotation** (orientation sensor, activity is re-created) | `rotateScreen_inputAndMetricAreKept_countStillWorks` |
| 7 | App goes to the **background** and returns | `appSentToBackgroundAndBack_resultIsKept` |

Scenarios 4-7 use device features: soft keyboard, system clipboard, screen rotation and the app lifecycle.

## Changes to the application code

- `TextCounter.count()`: removed the `default: throw new IllegalArgumentException(...)` branch.
  `TextMetric` has exactly 4 values, so this branch could never run (dead code)
  and made 100% coverage impossible. The app behaves exactly the same.
- `app/build.gradle`: test dependencies, `testInstrumentationRunner`, JaCoCo enabled.
- Added `gradlew`, `gradlew.bat`, `gradle-wrapper.jar` and `.gitignore`.

## Issue found during testing

After **screen rotation** the entered text and the selected metric are kept, but the **result disappears**,
because a `TextView` (`txtResult`) does not save its text by default.
Fix: add `android:freezesText="true"` to `txtResult` in `activity_main.xml`.
Test #6 checks what is kept and presses Count again after rotation.

## Conventions

- Meaningful names: `edtInputText`, `spnMetric`, `btnCount`, `txtResult`.
- Test names follow the pattern `method_condition_expectedResult`.
- Java coding standards: PascalCase classes, camelCase methods and variables,
  UPPER_SNAKE_CASE constants, 4-space indentation.
