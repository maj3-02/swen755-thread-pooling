# Thread pooling

Requires Java 21. Set `JAVA_HOME` to your JDK 21 installation.
The Gradle wrapper downloads Gradle and dependencies on the first run.

Run the application on Windows:

```powershell
.\gradlew.bat :app:run
```

On macOS or Linux:

```sh
bash ./gradlew :app:run
```

Build or run tests:

```powershell
.\gradlew.bat build
.\gradlew.bat test
```

On macOS or Linux, use `bash ./gradlew` instead of `.\gradlew.bat`.

The application currently prints a startup message. Prime searching and the
custom thread pool are not implemented yet. JUnit 4.13.2 is configured for tests.
