package com.smile.myboundserviceclient;

public class Constants {
    public static final String ServiceName = "com.smile.servicetest.MyBoundService";
    public static final String ServiceAction = "com.smile.servicetest.MyBoundService.START_SERVICE";
    public static final String PackageNameOfService = "com.smile.servicetest";
    public static final int ErrorCode = -1;
    public static final int ServiceStopped = 0;
    public static final int ServiceStarted = 1;
    public static final int ServiceBound = 2;
    public static final int ServiceUnbound = 3;
    public static final int MusicPlaying = 4;
    public static final int MusicPaused = 5;
    public static final int MusicStopped = 6;
    public static final int MusicLoaded = 7;
    public static final int StopService = 101;
    public static final int PlayMusic = 102;
    public static final int PauseMusic = 103;
    public static final int StopMusic = 104;
    public static final int AskStatus = 201;
    public static final int MusicStatus = 202;
    public static final int BinderIPC = 11;
    public static final int MessengerIPC = 12;

    public static final String Result = "RESULT";
    public static final String BinderOrMessenger = "BINDER_OR_MESSENGER";
    public static final String MyBoundServiceChannelName = "com.smile.servicetest.MyBoundService.ANDROID";
    public static final String MyBoundServiceChannelID = "com.smile.servicetest.MyBoundService.CHANNEL_ID";
    public static final int MyBoundServiceNotificationID = 1;

}
