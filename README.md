# App details :

Android application that provides real-time messaging, voice, and video calls for users.
Uses firebase firestore, firebase database (real time database) for storing user data, messages, online status and also provide notification via Firebase FCM.

Firebase FCM push is sent via our own Ktro server, server takes post request and checks validations if satified sents notification request to firebase fcm which then sends notification to users device even when the app is closed.

Firebase FCM is used as call invitation.
Supports foreground service enabling call to continue even if the app is closed and user can return to call screen anytime by clicking on the ongoing call notification.
Uses Agora SDK for handling voice and video call.

# future development and fixes to be done:
1. Enable group call.
2. Add group chat.
3. Fix the issue where notification is dismissable even though it was set as ongoing and ran using startForeground, make it undismissable till the call ends.
4. Increase fuildity of call screen ui and may be use call state instead, currently when the call ends for some reason due to delay or other reason for a split second user can see a UI which are shown when the remote user is not connected or call not accepted.
6. Improve UI look, provide UI customization options
7. Update firebase rules for better protection
8. Add end to to end Encryption 


# Technology and libraries used : 
Kotlin, Firebase, Hilt, Jetpack Compose, Android SDK, Agora SDK, Ktor,

# App images :
# Sign in and Sign up screen
Supports creating and logging via email and password or google account on the device.

<img src="https://github.com/user-attachments/assets/f2aec296-46f8-43bd-893c-caf6ba778031" alt="SignIn Screen" width="200"/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/a17ea255-1aa8-4ad7-b2d4-a2a99b042c74" alt="SignUp Screen" width="200"/>

# All chat screen and add friend screen:

User can see all their active chats on the all chat screen. Clicking on floating button navigates user to add friend screen.User can add new friends and initiate chat with them by adding them via this screen using friends email or uid.

<img src="https://github.com/user-attachments/assets/9abef91d-5a42-4305-98db-c3f8a8c13217" alt="All Chat Screen" width="200"/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/bb5bffac-9d03-49c4-9ff6-e74589461b2f" alt="Friend Screen" width="200"/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/5373f728-8d49-44e4-9073-c3dd170f271e" alt="Adding a Friend" width="200"/>

# Main chat screen, user profile and call history screen

<img src="https://github.com/user-attachments/assets/e1f34341-08ae-4ab5-9724-6f62346ab09b" alt="chat screen" width="200"/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/7b90c886-7213-4883-b4df-9b7e2f5f0bff" alt="profile screen" width="200"/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/241a4c76-0c74-4a01-aa79-76414d12e71f" alt="call history" width="200"/>

# Incoming and outgoing video call UI :
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/eb87eadd-392f-4a2e-9908-22d47cad9b87" alt="video call incoming and outgoin ui" width="800"/>

# Active video call UI
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/af646215-0f53-4972-b5f3-fd5cb0398655" alt=" active video call UI" width="800"/>
 

# Incoming and outgoing voice call UI :
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/67d21a4b-b80b-4f44-a44b-c8db67afc4ae" alt="voice call incoming and outgoin ui" width="800"/>
 

# Active Voice call UI :
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<img src="https://github.com/user-attachments/assets/38e27454-f1db-4c12-bb45-781b8287c372" alt="active voice call ui" width="800"/>

 

