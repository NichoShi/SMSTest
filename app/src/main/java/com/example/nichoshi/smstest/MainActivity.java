package com.example.nichoshi.smstest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView sender;
    TextView content;
    IntentFilter intentFilter;
    MySMSReceiver receiver;
    SendStatusReceiver sendStatusReceiver;
    EditText to;
    EditText text;
    Button sendBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sender = (TextView) findViewById(R.id.SenderTextView);
        content = (TextView) findViewById(R.id.contentTextView);
        to = (EditText) findViewById(R.id.toEditText);
        text = (EditText) findViewById(R.id.textEditText);
        sendBtn = (Button) findViewById(R.id.sendBtn);

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(100);
        receiver = new MySMSReceiver();
        registerReceiver(receiver,intentFilter);

        IntentFilter sendFilter = new IntentFilter();
        sendFilter.addAction("SENT_SMS_ACTION");
        sendStatusReceiver = new SendStatusReceiver();
        registerReceiver(sendStatusReceiver,sendFilter);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsManager manager = SmsManager.getDefault();
                Intent intent = new Intent("SENT_SMS_ACTION");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);
                manager.sendTextMessage(to.getText().toString(),null,text.getText().toString(),pendingIntent,null);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(sendStatusReceiver);


    }

    class MySMSReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            //String format = intent.getStringExtra("format"); for API level 23
            Object[] pdu = (Object[]) bundle.get("pdus");

            SmsMessage[] messages = new SmsMessage[pdu.length];
            for(int i = 0;i <messages.length; i++){
                messages[i] = SmsMessage.createFromPdu((byte[]) pdu[i]);
                // messages[i] = SmsMessage.createFromPdu((byte[]) pdu[i],format);  for API level 23
            }
            String Sender = messages[0].getOriginatingAddress();
            String Content = "";
            for(SmsMessage message:messages){
                Content += message.getMessageBody();
            }
            sender.setText(Sender);
            content.setText(Content);
            abortBroadcast();
        }
    }

    class SendStatusReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(getResultCode() == RESULT_OK){
                Toast.makeText(context,"send succeed!",Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context,"send failed!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
