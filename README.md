# UWB-RTLS-VR
This is the VR sister project of a native Android App which can be found here:https://github.com/mabaue/UWB-RTLS-Tool
More info can be found in the link to the native application above.

This app here includes a VR application which essentially wraps all logic from its sibling. That is, it creates a connection to a Tag inside a UWB-RTLS and continuously receives position updates from it. This data is filtered by a Kalman Filter before its applied on a simple VR App.
This app is part of my bachelor thesis which dealt with the question, how existing mobile 3DoF Virtual Reality Systems such as Google Cardboard can be extended to 6DoF. For this purpose, Ultra-Wideband technology was chosen.
Due to the Corona pandemic the UWB-RTLS was deployed in my shared-flat room, which explains why the virtual world contains some black objects, that essentially represent furniture in the room.

An example video which shows the performance can be found here:
https://www.youtube.com/watch?v=yA8zKvXQDIU&feature=youtu.be
