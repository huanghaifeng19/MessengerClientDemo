package com.cmbfae.messagerclientdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int COMMUNICATION_MSG = 1000;

    private Button mSendBnt;
    //显示连接状态
    private TextView mState;
    private Messenger mService;
    private boolean isConn;
    private EditText mInputContent;
    private TextView mReceiveMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //开始绑定服务
        bindServiceInvoked();

        mState = (TextView) findViewById(R.id.status);
        mSendBnt = (Button) findViewById(R.id.send_service);
        mInputContent = (EditText)findViewById(R.id.edit_text);
        mReceiveMessage =(TextView)findViewById(R.id.receive_message);

        mSendBnt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    String str = mInputContent.getText().toString();
                    Message msgFromClient = Message.obtain(null, COMMUNICATION_MSG, 2,3);
                    msgFromClient.replyTo = mMessenger;
                    if (isConn)
                    {
                        //往服务端发送消息
                        mService.send(msgFromClient);
                    }
                } catch (RemoteException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    private Messenger mMessenger = new Messenger(new Handler()
    {
        @Override
        public void handleMessage(Message msgFromServer)
        {
            switch (msgFromServer.what)
            {
                case COMMUNICATION_MSG:
                    mReceiveMessage.setText(msgFromServer.arg1+"");
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });


    private ServiceConnection mConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mService = new Messenger(service);
            isConn = true;
            mState.setText("connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
            isConn = false;
            mState.setText("disconnected!");
        }
    };

    private void bindServiceInvoked()
    {
        Intent intent = new Intent();
        intent.setAction("com.henry.aidl");
        bindService(createExplicitFromImplicitIntent(this,intent), mConn, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "bindService invoked !");
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(mConn);
    }

}
