package com.example.messengerservicesample;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class MessengerSampleService extends Service {

    private static final String TAG = "MessengerSampleService";

    // この値を双方でそろえないといけない。
    public static final int MSG_REGISTER_CLIENT = 0xdead;
    public static final int MSG_UNREGISTER_CLIENT = 0xbeef;
    public static final int MSG_SET_VALUE = 0x123;
    public static final int MSG_TEXT_UPDATE = 0xDAAAA;

    private Messenger messenger = new Messenger(new IncomingHandler(Looper.getMainLooper()));

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return messenger.getBinder();
    }

    static class IncomingHandler extends Handler {

        private IncomingHandler(@NonNull Looper looper) {
            super(looper);
        }

        private ArrayList<Messenger> clients = new ArrayList<>();
        private int cnt = 0;
        int val;
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    clients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    clients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
//                    Toast.makeText(MessengerSampleService.this, "Received message!!\nMessengerSampleServiceより愛を込めて。。", Toast.LENGTH_LONG).show();
                    // ActivityからMessageを受け取った2秒後にActivityに向けて通知送信
                    HandlerThread thread = new HandlerThread("hoge");
                    thread.start();
                    val = msg.arg1;
                    new Handler(thread.getLooper()).postDelayed(() -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("key", "ABLE TO SEND ANY PARCELABLE OBJECT NYAAAAAAA!!!");
                            for (int i = clients.size() - 1; i >= 0; i--) {
                                try {
                                    clients.get(i).send(Message.obtain(null,
                                            MSG_TEXT_UPDATE, (val + cnt), 0, bundle));
                                } catch (RemoteException e) {
                                    clients.remove(i);
                                }
                            }
                            cnt++;
                    }, 2000);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
