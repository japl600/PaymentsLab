# Payments Lab

A lab for practicing and testing the code exercises from payments interview prep. A single-module Android app
(Kotlin, Compose) with one tab per exercise, plus JVM unit tests that run without an emulator.

| Tab | Exercise | Code | Tests |
|---|---|---|---|
| Serial frame | `FrameParser.feed`: STX, LEN, DATA, ETX, LRC; fragmentation, garbage, resynchronization | `framing/FrameParser.kt` | `FrameParserTest`, `FrameBuilderTest` |
| TLV (EMV) | `parseTlv`: multibyte tags, 0x81 and 0x82 lengths, truncated data, constructed tags | `emv/Tlv.kt` | `TlvTest` |
| PAN | `maskPan` and `isValidLuhn` | `pan/Pan.kt` | `PanTest` |

## How to try it

- **Tests (fastest):** `.\gradlew.bat test`. The report is written to `app/build/reports/tests/testDebugUnitTest/index.html`.
- **App:** open this folder in Android Studio and run `app` on an emulator or device (`minSdk 26`). Each tab has
  ready-made test cases and lets you type your own.
- **Tip:** in "Serial frame", paste half a frame, press *Feed*, paste the other half and press again: the parser
  keeps its buffer between presses.

## Notes

- Versions copied from Repzi: AGP 9.4.1, Kotlin 2.4.21, Gradle 9.8.2, Compose BOM 2026.09.00. JDK 21 from the terminal.
- If Android Studio generates `gradle/gradle-daemon-jvm.properties` (pins JDK 25) and the terminal build fails with
  "Unable to download toolchain", delete that file. It is in `.gitignore`.
- Use test card numbers only.

## Ideas to extend it

- `SerialClient` with `Channel` and `CompletableDeferred` against a fake port.
- Exponential backoff with jitter and its `runTest` test.
- A persistent, atomic STAN generator.
