Camera Tester
============

Details different photo capture implementations on Android.  Some work on certain devices, some don't.

Please fork, fix issues you see with any of these, and most importantly consider adding examples of other strategies.  Leveraging the camera in Android can be harder than it should be.

**File URI**
Lifted verbatim from http://developer.android.com/guide/topics/media/camera.html . This implementation attempts to pass the camera activity a URI for a file in external storage.  This seems to return a null URI on some devices.

**Content Resolver**
Uses MediaStore.Images.Media.EXTERNAL_CONTENT_URI to generate a URI for the Camera activity.  This seems to be stable across many devices, though it's not an implementation that is easily found online.  Thanks to iNaturalist source code for kernal of source here.

**Homegrown Camera**
When all else fails, you can just implement your own camera.  Not so feature rich, but it seems to work across many devices
