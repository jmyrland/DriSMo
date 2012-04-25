-------------------------------------------------------------------------------

                        ____        _  _____  _____      
                       |    \  ___ |_||   __||     | ___ 
                       |  |  ||  _|| ||__   || | | || . |
                       |____/ |_|  |_||_____||_|_|_||___|

           ___                                                       
            | |_  _    _|._ o   o._  _    _.    _.|o_|_      _.._ ._ 
            | | |(/_  (_||  |\/ || |(_|  (_||_|(_||| |_ \/  (_||_)|_)
                                     _|    |            /      |  |  
                                                         
-------------------------------------------------------------------------------

The basis of DriSMo was developed as a bachelor project in 2011, by three 
students at Gj�vik University College (Fredrik Kvitvik, Fredrik H�rtvedt and
J�rn Andr� Myrland). For documentation on DriSMo.

DriSMo is free software; you can redistribute it and/or modify it under the 
terms of the GNU General Public License as published by the Free Software 
Foundation; either version 3 of the License, or (at your option) any later 
version.

-------------------------------------------------------------------------------

This Android application has been developed by using the IntelliJ IDEA as the 
IDE, which supports development of Android applications. All the neccessary 
source files are provided to build a running version of DriSMo.

You are also required to download the Android SDK, and integrate it with
IntelliJ IDEA. The SDK is found at http://developer.android.com/sdk/index.html
More info at http://wiki.jetbrains.net/intellij/Android

To run the project in the IntelliJ IDEA IDE:
  0. Download the a version of the IDE here: http://www.jetbrains.com/idea/
  1. Launch IntelliJ IDEA, and create a new project "file->New Project".
  2. Select "Create Java project from existing sources".
  3. Fill in name "DriSMo", and set the path to project files location
     as the folder you opened this file from. In my case, this is the  
	 "source code" folder.
  4. Next, you will see a message which tells you that IDEA has found some
     folders containing source files. Mark all folders, and proceed.
  5. The next step will located all related libraries. Check the "libs", and
     proceed.
  6. The next two steps, just check all boxes and proceed to finish the setup.

-------------------------------------------------------------------------------

Below is a commented tree structure of the provided folder:

Drismo
  +---JavaDoc                <- Java documentation of DriSMo (open index.html)
  +---assets                 <- Fonts or other assets.
  +---bin                    <- Compiled versions of the application (APK-file)
  +---gen                    <- Generated files by Android.
  |   +---com
  |       +---drismo
  +---libs                   <- External libs
  +---res                    <- Graphics/language/layout root folder.  
  |   +---anim
  |   +---drawable
  |   +---drawable-hdpi
  |   +---drawable-ldpi
  |   +---drawable-mdpi
  |   +---drawable-notlong
  |   +---layout
  |   +---layout-no
  |   +---menu
  |   +---values
  |   +---values-en
  |   +---values-no
  |   +---xml
  +---src                    <- The actual source folder w/ sub packages.
      +---com
          +---drismo
              +---facebook
              +---gui
              |   +---monitor
              |   +---quickaction
              +---logic
              |   +---sms
              +---model

-------------------------------------------------------------------------------

RELEASE NOTES

1.0.3 DriSMo STABLE
- Calibration fixed on devices with "unreliable" sensors.

1.0.2 DriSMo STABLE
- The messenger is now only active while the monitor is running.

1.0 DriSMo STABLE
- Export function improved/fixed
- Map bug fixed
- Accessability improved

0.9.2 DriSMo BETA
- Calibration bug on some devices is fixed.

0.9 DriSMo BETA
- Updated Facebook integration to include sharing images.

0.8.6 DriSMo BETA
- Fixed calibration bug
- Added Facebook integration

0.8.5 DriSMo BETA
- Fixed duplication bug in DriSMo Messenger (on calls).
- Added one click access to trip graph from archive.

0.8.4 DriSMo BETA
- Multi-language support.
- Possibility to abort calibration.
- Some new GUI-animations.

-------------------------------------------------------------------------------

KNOWN BUGS

1) Some devices can't show their location settings in landscape view.
DriSMo will crash on these devices under these special circumstances:
Your application settings say you want to use the GPS, but the GPS is not
activated in your device when you start the DriSMo monitor. If you at this
point don't turn on the GPS, but rather press back and continue the monitor
activity, you can run in to some problems. When switching to wheel-view while
the monitor is already running with the device locked in landscape view,
the application will crash.

-------------------------------------------------------------------------------

All graphics used in DriSMo is either created explicit for the application, or
it is downloaded from http://www.iconspedia.com/ .

-------------------------------------------------------------------------------
                        ____        _  _____  _____      
                       |    \  ___ |_||   __||     | ___ 
                       |  |  ||  _|| ||__   || | | || . |
                       |____/ |_|  |_||_____||_|_|_||___|
                                 
                                     CREDITS:

                                 Fredrik Kvitvik
                                 Fredrik H�rtvedt
                                J�rn Andr� Myrland
								
-------------------------------------------------------------------------------