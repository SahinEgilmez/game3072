# game3072

A [libGDX](https://libgdx.com/) puzzle game generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This repository implements a sliding tile puzzle inspired by **2048** with a target of 3072. The shared game logic lives in the `core` module and is used by the desktop, Android and iOS launchers.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `android`: Android mobile platform. Needs Android SDK.
- `ios`: iOS mobile platform using RoboVM.

## Code overview

The `core` module defines the game:

- **`Main`** sets up rendering, input and game state.
- **`Grid`** holds board values, processes moves and animations.
- **`Tile`** draws individual numbered tiles.
- **`GameUtils`** provides small helpers for fonts and drawing.

Platform folders contain launchers that invoke `Main` on desktop (`lwjgl3`), Android and iOS.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Running locally

To start the desktop version from the command line:

```bash
./gradlew lwjgl3:run
```

Android and iOS launchers can be assembled with the corresponding Gradle tasks once their SDKs are configured.
