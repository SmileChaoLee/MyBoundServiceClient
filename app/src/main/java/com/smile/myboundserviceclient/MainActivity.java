package com.smile.myboundserviceclient;

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

    private final String TAG = "MainActivity";
    private TextView messageText;
    private Button bindServiceButton;
    private Button unbindServiceButton;
    private Button playButton;
    private Button pauseButton;
    private Button exitBoundService;
    private boolean isServiceBound = false;
    private Messenger sendMessenger;
    private IBinder.DeathRecipient deathRecipient;
    private ServiceConnection myServiceConnection;
    // private Context context;

    private Messenger clientMessenger = new Messenger(new ClientHandler());
    private class ClientHandler extends Handler {
        public ClientHandler() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"handleMessage.msg.what = " + msg.what);
            switch (msg.what) {
                case Constants.ServiceStopped:
                    Log.d(TAG,"ServiceStopped received");
                    messageText.setText("BoundService stopped.");
                    bindServiceButton.setEnabled(false);
                    unbindServiceButton.setEnabled(false);
                    playButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    break;
                case Constants.ServiceStarted:
                    Log.d(TAG,"ServiceStarted received");
                    messageText.setText("BoundService started.");
                    bindServiceButton.setEnabled(true);
                    unbindServiceButton.setEnabled(false);
                    playButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    break;
                case Constants.MusicPlaying:
                    Log.d(TAG,"MusicPlaying received");
                    messageText.setText("Music playing.");
                    if (isServiceBound) {
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(true);
                    }
                    break;
                case Constants.MusicPaused:
                    Log.d(TAG,"MusicPaused received");
                    messageText.setText("Music paused.");
                    if (isServiceBound) {
                        playButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                    }
                    break;
                case Constants.MusicStopped:
                    Log.d(TAG,"MusicStopped received");
                    messageText.setText("Music stopped.");
                    if (isServiceBound) {
                        playButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                    }
                    break;
                case Constants.MusicLoaded:
                    Log.d(TAG,"MusicLoaded received");
                    messageText.setText("Music Loaded.");
                    if (isServiceBound) {
                        playButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                    }
                    break;
                case Constants.MusicStatus:
                    Log.d(TAG,"MusicStatus received.isServiceBound = "
                            + isServiceBound);
                    if (isServiceBound) {
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(false);
                        if (msg.arg1 == 1) {    // Music Loaded
                            Log.d(TAG,"MusicStatus music loaded");
                            playButton.setEnabled(msg.arg2 != 1);
                            pauseButton.setEnabled(msg.arg2 == 1);
                        }
                    }
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // context = getApplicationContext();
        messageText = findViewById(R.id.messageText);
        bindServiceButton = findViewById(R.id.bindServiceButton);
        bindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start service
                bindMusicService();
            }
        });
        unbindServiceButton = findViewById(R.id.unbindServiceButton);
        unbindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindMusicService();
            }
        });
        playButton = findViewById(R.id.playMusic);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((sendMessenger != null) && (isServiceBound)) {
                    // play music
                    Message msg = Message.obtain(null, Constants.PlayMusic, 0, 0);
                    try {
                        msg.replyTo = clientMessenger;
                        sendMessenger.send(msg);
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
                    Message msg = Message.obtain(null, Constants.PauseMusic, 0, 0);
                    try {
                        msg.replyTo = clientMessenger;
                        sendMessenger.send(msg);
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

        deathRecipient = new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                Log.d(TAG, "binderDied");
            }
        };

        myServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(TAG, "onServiceConnected");
                messageText.setText("BoundService bound.");
                sendMessenger = new Messenger(binder);
                isServiceBound = true;
                bindServiceButton.setEnabled(false);
                unbindServiceButton.setEnabled(true);
                Message msg = Message.obtain(null, Constants.AskStatus, 0, 0);
                try {
                    msg.replyTo = clientMessenger;
                    sendMessenger.send(msg);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                messageText.setText("BoundService bound.");
                sendMessenger = null;
                isServiceBound = false;
                bindServiceButton.setEnabled(true);
                unbindServiceButton.setEnabled(false);
                playButton.setEnabled(false);
                pauseButton.setEnabled(false);
            }
        };

        bindMusicService();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        unbindMusicService();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void unbindMusicService() {
        Log.d(TAG, "unbindMusicService");
        Toast.makeText(this, "Unbinding ...", Toast.LENGTH_SHORT).show();
        if (isServiceBound) {
            unbindService(myServiceConnection);
            isServiceBound = false;
            bindServiceButton.setEnabled(true);
            unbindServiceButton.setEnabled(false);
            playButton.setEnabled(false);
            pauseButton.setEnabled(false);
            messageText.setText("BoundService bound.");
        }
    }

    // bind to the service
    private void bindMusicService() {
        Log.d(TAG, "bindMusicService");
        Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show();
        if (!isServiceBound) {
            // Intent bindServiceIntent = new Intent(serviceName);
            // must set the package name of bound service
            // bindServiceIntent.setPackage(PackageNameOfService);
            // or
            Intent bindServiceIntent = new Intent();
            bindServiceIntent.setComponent(new ComponentName(Constants.PackageNameOfService
                    , Constants.ServiceName));
            // parameters for this Intent
            Bundle extras = new Bundle();
            extras.putInt(Constants.BinderOrMessenger, Constants.MessengerIPC);
            bindServiceIntent.putExtras(extras);
            isServiceBound = bindService(bindServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void stopMusicService() {
        Log.d(TAG, "stopMusicService");
        Message msg = Message.obtain(null, Constants.ServiceStopped, 0, 0);
        try {
            msg.replyTo = clientMessenger;
            sendMessenger.send(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
