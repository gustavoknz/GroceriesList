This is a Kotlin Multiplatform project targeting Android and Web. It also implements the Ktor Server backend.

* [/app/shared](./app/shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./app/shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.

* [/core](./core/src) is for the code that will be shared between all targets in the project.
  - [ApiConfig.android.kt](./core/src/androidMain/kotlin/com/kieling/groceries/ApiConfig.android.kt): Configures the API base URL (`http://10.0.2.2:8080` for emulators or your local network IP `http://192.168.x.x:8080` for physical devices).
  - [GroceryItem.kt](./core/src/commonMain/kotlin/com/kieling/groceries/domain/model/GroceryItem.kt): Domain model annotated with `@Serializable` for Ktor serialization.

* [/server](./server/src/main/kotlin) is for the Ktor server application.
  - [Application.kt](./server/src/main/kotlin/com/kieling/groceries/Application.kt): Ktor server implementation featuring file-based JSON persistence (`items.json`) so data survives server restarts.

### Android Setup & Network Configuration
- **Internet Permission & Cleartext Traffic**: [AndroidManifest.xml](./app/androidApp/src/main/AndroidManifest.xml) includes `android.permission.INTERNET` and `android:usesCleartextTraffic="true"` to allow HTTP communication with the server.
- **Base URL**: 
  - For Android Emulator: Uses `http://10.0.2.2:8080`.
  - For Physical Devices: Update `getApiBaseUrl()` in `ApiConfig.android.kt` to your computer's local Wi-Fi IP address (e.g. `http://192.168.1.x:8080`), and ensure both devices are on the same Wi-Fi network.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :app:androidApp:assembleDebug`
- Server (with auto-persistence to `items.json`): `./gradlew :server:run`
- Web app:
  - Wasm target (faster, modern browsers): `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
  - JS target (slower, supports older browsers): `./gradlew :app:webApp:jsBrowserDevelopmentRun`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :app:shared:testAndroidHostTest`
- Server tests: `./gradlew :server:test`
- Web tests:
  - Wasm target: `./gradlew :app:shared:wasmJsTest`
  - JS target: `./gradlew :app:shared:jsTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).
