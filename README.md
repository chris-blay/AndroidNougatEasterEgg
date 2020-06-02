# Android Nougat Easter Egg (a.k.a. "Android Neko")

I wanted to keep playing this game on Android O, so lifted the source from
AOSP, cleaned it up a bit, fixed some things, put it in a Bazel workspace,
and published it on Google Play Store.

## How to install

* Get it from the Google Play Store at
  https://play.google.com/store/apps/details?id=com.covertbagel.neko
* For Android Nougat (7), Oreo (8), and Pie (9): Download
  `AndroidNougatEasterEgg.apk` and sideload it. It's exactly
  the same as the version published on Play Store.
* For Android 10 and up: Download `AndroidNougatEasterEggSdk29.apk` and
  sideload it. It's exactly the same as the version published on Play Store.

## How to keep your cats

There is experimental support for teleporting cats from the Android system
easter egg app to this one in `teleport_cats`. It has only been tested on
Debian-based Linux distributions but hopefully works on Mac OS X and Cygwin
too.

It requires a Java runtime, Android SDK platform tools (really just ADB),
and Android Backup Extractor JAR available at
https://sourceforge.net/projects/adbextractor/.

## How to build

This project uses Bazel to build. You'll need to get Bazel setup first but
then it's really easy to build.

- Android SDK 29, build tools 29.0.2, and Android Support Library 25.3.1 need
  to be in your local Android SDK installation.
- This project is configured to look for your Android SDK installation at
  `/opt/android-sdk` but it's easy enough to change that to wherever you like.
  Just modify the one reference in `workspace/WORKSPACE`.
- Go to `workspace` and run `bazel build :AndroidNougatEasterEggDev`.

## How to hack

You _could_ just edit with your text editor of choice and use Bazel directly
from the command line...

...but also it's pretty straightforward to import this Bazel project into
Android Studio. Just install the Bazel plugin for Android Studio, use the
"Import Bazel Project" option, point to `workspace` for the workspace,
use `workspace/.bazelproject`, and create a project directory here called
`AndroidNougatEasterEgg` (already in .gitignore) to use.
