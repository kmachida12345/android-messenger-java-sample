package com.example.messengeractivitysample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class MessengerSampleActivity extends Activity {

    private static final String TAG = "MessengerSampleActivity";

    private final Messenger messenger = new Messenger(new IncomingHandler(Looper.getMainLooper()));
    private Messenger service;

    private boolean isBound;
    private TextView textView;

    // この値を揃える必要がある。
    public static final int MSG_REGISTER_CLIENT = 0xdead;
    public static final int MSG_UNREGISTER_CLIENT = 0xbeef;
    public static final int MSG_SET_VALUE = 0x123;
    public static final int MSG_TEXT_UPDATE = 0xDAAAA;

    final private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            MessengerSampleActivity.this.service = new Messenger(service);
            textView.append("Attached\n");
            try {
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = messenger;
                MessengerSampleActivity.this.service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Toast.makeText(MessengerSampleActivity.this, "onServiceConnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            isBound = false;
            textView.append("Disconnected\n");
            Toast.makeText(MessengerSampleActivity.this, "onServiceDisconnected", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.hoge);

        findViewById(R.id.button_bind).setOnClickListener(v -> doBindService());
        findViewById(R.id.button_unbind).setOnClickListener(v -> doUnbindService());
        findViewById(R.id.button_send_message).setOnClickListener(v -> {
            if (service == null) {
                Toast.makeText(MessengerSampleActivity.this, "先にバインドしてな", Toast.LENGTH_LONG).show();
                return;
            }
            textView.append("sending message\n");
            // Give it some value as an example.
            Message msg = Message.obtain(null,
                    MSG_SET_VALUE, 1234, 0);
            try {
                service.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    static class IncomingHandler extends Handler {

//        private TextView textView;
        private IncomingHandler(@NonNull Looper looper/*, TextView textView*/) {
            super(looper);
//            this.textView = textView;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TEXT_UPDATE:
//                    textView.append("Received from service: received values:" + msg.arg1 + ", " + ((Bundle) msg.obj).getString("key") + "\n");
                    Log.d(TAG, "handleMessage: received values:" + msg.arg1 + ", " + ((Bundle) msg.obj).getString("key"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    void doBindService() {
        Intent intent = new Intent("com.example.messengerservicesample.HOGE");
        intent.setPackage("com.example.messengerservicesample");
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        isBound = true;
        textView.append("Binding\n");
    }

    void doUnbindService() {
        if (isBound) {
            if (service != null) {
                Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
                msg.replyTo = messenger;
                try {
                    service.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            service = null;
            unbindService(mConnection);
            isBound = false;
            textView.append("Unbinding\n");
        }
    }
}
