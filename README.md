### Stickers

A playground android app for recording videos and overlaying stickers.

#### Done:

- Recording video via the system camera app
- Configured [FFMpeg](https://github.com/writingminds/ffmpeg-android-java) for doing video operations
- Passing the recorded video through FFMpeg commands to generate some output (a simple identity filter for now)
- Saving the output to disk (hardcoded to "copy.mp4" in the external disk root)

#### To be done:

- Add some sticker resources to the app
- Allow for positioning inside the video frame
- Record the sticker position with respect to the video attributes
- Use the above information to overlay the sticker on every input frame
- Ask the user for save target, instead of a hardcoded random location


#### Running:

Simply open the app in Android studio, and hit run. 

Note that you cannot run the app in an emulator with API >= 23. This is because the current FFMPeg binary uses [text relocations](https://android.googlesource.com/platform/bionic/+/master/android-changes-for-ndk-developers.md#text-relocations-enforced-for-api-level-23). Google escalated this performance warning to a failure starting API 23. This is a [known issue](https://issuetracker.google.com/issues/37067983). It's possible that this issue only affects x86 emulators, and not actual ARM devices, based on [this stack overflow comment](https://stackoverflow.com/a/50207091). But need to research more. 