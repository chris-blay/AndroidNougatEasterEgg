# Android Nougat Easter Egg (a.k.a. "Android Neko")

I wanted to keep playing this game on Android O, so lifted the source from
AOSP, cleaned it up a bit, fixed sharing, and put it in a Bazel workspace.

AFAIK it's not possible to transfer cats from the system app on Nougat to
this regular app (since they have different package names) but it's also
possible to install/use this on Nougat directly so that all your hard cat
efforts do not go to waste later when upgrading Android.

## How to build

This project uses Bazel to build. You'll need to get Bazel setup first but
then it's really easy to build.

- Android SDK 24, build tools 26.0.1, and Android Support Library need to be
  in your local Android SDK installation.
- This project is configured to look for your Android SDK installation at
  `/opt/android-sdk` but it's easy enough to change that to wherever you like.
  Just modify the two references in `workspace/WORKSPACE`.
- Go to `workspace` and run `bazel build :AndroidNougatEasterEgg`.

## How to install

`bazel mobile-install :AndroidNougatEasterEgg`

## How to hack

You _could_ just edit with your text editor of choice and use Bazel directly
from the command line...

...but also it's pretty straightforward to import this Bazel project into
Android Studio. Just install the Bazel plugin for Android Studio, use the
"Import Bazel Project" option, point to `workspace` for the workspace,
use `workspace/.bazelproject`, and create a project directory here called
`AndroidNougatEasterEgg` (already in .gitignore) to use.

Some of the support library things don't seem to show up in Android Studio,
probably because they are in AAR files and that isn't supported yet, but
all the rest of refactoring, organizing imports, linting, etc. works.

