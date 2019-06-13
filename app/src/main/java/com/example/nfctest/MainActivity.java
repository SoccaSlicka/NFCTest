package com.example.nfctest;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.*;

import android.nfc.cardemulation.HostApduService;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;

import java.io.*;
import java.lang.String;

import android.widget.TextView;
import android.view.View;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.NFC,
    };

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";
    public static final String NFC_ERROR = "Error while transmitting, is the NFC tag close enough to your device?";
    public String text;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;

    TextView tvNFCContent;
    TextView message;
    EditText editText = null;
    Button generate = null;
    Button beam = null;
    TextView gtext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        editText = (EditText) findViewById(R.id.editText);
        generate = (Button) findViewById(R.id.Generate);
        beam = (Button) findViewById(R.id.Beam);
        gtext = (TextView) findViewById(R.id.gText);

        checkPermissions();

        generate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    text = editText.getText().toString();

                    //converts entered text to bytes, then to hex, then passes the first 8 characters to the message variable
                    String nfcmessage = String.format("%8X", new BigInteger(1, text.getBytes("UTF-8"))).substring(0, 8);
                    String a1 = nfcmessage.substring(0, 2);
                    String a2 = nfcmessage.substring(2, 4);
                    String a3 = nfcmessage.substring(4, 6);
                    String a4 = nfcmessage.substring(6, 8);

                    gtext.setText(a1 + " " + a2 + " " + a3 + " " + a4);
                } catch (IOException ie) {
                    ie.printStackTrace();
                }

            }
        });

        tvNFCContent = (TextView) findViewById(R.id.nfc_contents);
        message = (TextView) findViewById(R.id.gText);
        beam = (Button) findViewById(R.id.Beam);

        beam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startService(new Intent(context, HostCardEmulatorService.class));
                    }
                }).start();

            }
        });


    }

    public class HostCardEmulatorService extends HostApduService {
      /*public int onStartCommand(Intent intent, int flags, int startId)
        {
         IntentFilter filter = new IntentFilter();
         filter.addAction("android.nfc.cardemulation.action.HOST_APDU_SERVICE");
         filter.addCategory("com.example.nfctest");
         return 1;
        }*/

        String TAG = "Host Card Emulator";
        String STATUS_SUCCESS = "9000";
        String STATUS_FAILED = "6F00";
        String CLA_NOT_SUPPORTED = "6E00";
        String INS_NOT_SUPPORTED = "6D00";
        String AID = "A0000002471001";
        String SELECT_INS = "A4";
        String DEFAULT_CLA = "00";
        Integer MIN_APDU_LENGTH = 12;

        @Override
        public byte[] processCommandApdu(byte[] apdu, Bundle extras) {

            tvNFCContent.setText(String.format("%40X", apdu));

            byte[] command = null;
            try {
                command = text.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e ) {
                tvNFCContent.setText(NFC_ERROR);
            }

            return command;
        }
        @Override
        public void onDeactivated(int reason) {
            Toast.makeText(this, NFC_ERROR, Toast.LENGTH_LONG).show();


        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            }
            return;
        }
    }

}
