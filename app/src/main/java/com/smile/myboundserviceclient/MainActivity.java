package com.smile.myboundserviceclient;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private String TAG = "com.smile.myboundserviceclient.MainActivity";

    private final String serviceAction = "com.smile.servicetest.MyBoundService.START_SERVICE";
    private final String serviceName = "com.smile.servicetest.MyBoundService";
    private final String packageNameOfService = "com.smile.servicetest";

    private final int ServiceStopped = 0x00;
    private final int ServiceStarted = 0x01;
    private final int MusicPlaying = 0x02;
    private final int MusicPaused = 0x03;
    private final int BinderIPC = 1;
    private final int MessengerIPC = 2;

    private TextView messageText;
    private Button startBindServiceButton;
    private Button unbindStopServiceButton;
    private Button playButton;
    private Button pauseButton;
    private Button exitBoundService;
    private boolean isServiceBound = false;
    private Messenger sendMessenger;
    private ServiceConnection myServiceConnection;

    private Messenger clientMessenger = new Messenger(new ClientHandler());
    private class ClientHandler extends Handler {
        public ClientHandler() {
            super(Looper.myLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // case MyBoundService.ServiceStopped:
                case ServiceStopped:
                    Log.d(TAG,"ServiceStopped received");
                    messageText.setText("BoundService stopped.");
                    break;
                // case MyBoundService.ServiceStarted:
                case ServiceStarted:
                    Log.d(TAG,"ServiceStarted received");
                    messageText.setText("BoundService started.");
                    break;
                // case MyBoundService.MusicPlaying:
                case MusicPlaying:
                    Log.d(TAG,"MusicPlaying received");
                    messageText.setText("Music playing.");
                    break;
                // case MyBoundService.MusicPaused:
                case MusicPaused:
                    Log.d(TAG,"MusicPaused received");
                    messageText.setText("Music paused.");
                    break;
                default:
                    Log.d(TAG,"Unknown message");
                    messageText.setText("Unknown message.");
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageText = findViewById(R.id.messageText);
        startBindServiceButton = findViewById(R.id.startBindService);
        startBindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start service
                startMusicService();
                doBindToService();
            }
        });
        unbindStopServiceButton = findViewById(R.id.unbindStopService);
        unbindStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindAndStopMusicService();
            }
        });
        playButton = findViewById(R.id.playMusic);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((sendMessenger != null) && (isServiceBound)) {
                    // play music
                    // Message msg = Message.obtain(null, MyBoundService.MusicPlaying, 0, 0);
                    Message msg = Message.obtain(null, MusicPlaying, 0, 0);
                    try {
                        msg.replyTo = clientMessenger;
                        sendMessenger.send(msg);
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(true);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        pauseButton = findViewById(R.id.pauseMusic);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((sendMessenger != null) && (isServiceBound)) {
                    // pause music
                    // Message msg = Message.obtain(null, MyBoundService.MusicPaused, 0, 0);
                    Message msg = Message.obtain(null, MusicPaused, 0, 0);
                    try {
                        msg.replyTo = clientMessenger;
                        sendMessenger.send(msg);
                        playButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        exitBoundService = findViewById(R.id.exitBoundService);
        exitBoundService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // finish activity
            }
        });

        myServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected()");
                sendMessenger = new Messenger(service);
                isServiceBound = true;

                // send message to MyBoundService
                // Message msg = Message.obtain(null, MyBoundService.ServiceStarted, 0, 0);
                Message msg = Message.obtain(null, ServiceStarted, 0, 0);
                try {
                    msg.replyTo = clientMessenger;
                    sendMessenger.send(msg);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected()");
                sendMessenger = null;
                isServiceBound = false;
            }
        };

        // start service
        startMusicService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        doBindToService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unbind from the service
        Log.d(TAG, "onPause()");
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroying activity .......");
        if (isFinishing()) {
            // stop service as activity being destroyed and we won't use any more
            Log.d(TAG, "Activity is finishing.");
            stopMusicService();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void startMusicService() {
        // start service
        // if (!MyBoundService.isServiceRunnding) {
        try {
            // Intent serviceIntent = new Intent(serviceAction);
            // serviceIntent.setPackage(packageNameOfService);  // must set the package name of bound service
            // or
            Intent serviceIntent = new Intent();
            serviceIntent.setComponent(new ComponentName(packageNameOfService, serviceName));
            // parameters for this Intent
            Bundle extras = new Bundle();
            extras.putInt("BINDER_OR_MESSENGER", MessengerIPC);
            serviceIntent.putExtras(extras);
            startService(serviceIntent);

            startBindServiceButton.setEnabled(false);
            unbindStopServiceButton.setEnabled(true);
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
            // MyBoundService.isServiceRunning = true;
            // isServiceRunning.setBoolean(serviceClass, true);
            Log.d(TAG, "startMusicService.Service started");
        } catch (Exception ex) {
            Log.d(TAG, "startMusicService.Exception");
            ex.printStackTrace();
        }
    }

    // bind to the service
    private void doBindToService() {

        // if (MyBoundService.isServiceRunning) {
        // service is running
        Log.d(TAG, "doBindToService");
        Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show();
        if (!isServiceBound) {
            // Intent bindServiceIntent = new Intent(serviceAction);
            // bindServiceIntent.setPackage(packageNameOfService);  // must set the package name of bound service
            // or
            Intent bindServiceIntent = new Intent();
            bindServiceIntent.setComponent(new ComponentName(packageNameOfService, serviceName));
            // parameters for this Intent
            Bundle extras = new Bundle();
            extras.putInt("BINDER_OR_MESSENGER", MessengerIPC);
            bindServiceIntent.putExtras(extras);
            Log.d(TAG, "doBindToService.myServiceConnection = " + myServiceConnection);
            isServiceBound = bindService(bindServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "doBindToService.isServiceBound = " + isServiceBound);
        }
        // }
    }

    private void unbindAndStopMusicService() {
        // start service
        try {
            doUnbindService();
            stopMusicService();
            startBindServiceButton.setEnabled(true);
            unbindStopServiceButton.setEnabled(false);
            playButton.setEnabled(false);
            pauseButton.setEnabled(false);
            Log.d(TAG, "unbindAndStopMusicService");
        } catch (Exception ex) {
            Log.d(TAG, "unbindAndStopMusicService.Exception");
            ex.printStackTrace();
        }
    }

    // unbind from the service
    private void doUnbindService() {
        // if (MyBoundService.isServiceRunning) {
            // service is running
            Log.d(TAG, "doUnbindService");
            Toast.makeText(this, "Unbinding ...", Toast.LENGTH_SHORT).show();
            if (isServiceBound) {
                unbindService(myServiceConnection);
                isServiceBound = false;
            }
        // }
    }

    private void stopMusicService() {
        // if (MyBoundService.isServiceRunning) {
            Log.d(TAG, "stopMusicService");
            // Intent serviceStopIntent = new Intent(this, MyBoundService.class);
            // stopService(serviceStopIntent);
            // Message msg = Message.obtain(null, MyBoundService.ServiceStopped, 0, 0);
            Message msg = Message.obtain(null, ServiceStopped, 0, 0);
            try {
                msg.replyTo = clientMessenger;
                sendMessenger.send(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // MyBoundService.isServiceRunning = false; // set inside onDestroy() inside MyBoundService.class
        // }
    }

    // deprecated
    private boolean isServiceRunning(Class<?> serviceClass) {
        Log.d(TAG, "isServiceRunning");
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                Log.d(TAG, "isServiceRunning.true");
                return true;
            }
        }
        Log.d(TAG, "isServiceRunning.false");
        return false;
    }
}
