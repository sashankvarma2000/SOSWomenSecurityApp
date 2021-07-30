package com.example.task2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    BackgroundService.BackgroundBinder mService;
    boolean mBound = false;
    private double longitude = 0, latitude = 0;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendmsg = findViewById(R.id.button);
        Button enable = findViewById(R.id.enable);
        Button disable = findViewById(R.id.disable);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                        {Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION},
                PackageManager.PERMISSION_GRANTED);


        Button viewAndUpdateContacts = findViewById(R.id.contacts);


        viewAndUpdateContacts.setOnClickListener(V -> {
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
        });


        sendmsg.setOnClickListener(V -> {
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED||
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.e("fused location provider latitude",String.valueOf(latitude) );
                            Log.e("fused location provider longitude",String.valueOf(longitude) );
                            sendSMS();
                        }
                    }
                });
            }
            else
            {
                Log.e("location access","not given" );
            }
        });

        enable.setOnClickListener(v -> {
            Intent intent = new Intent(this, BackgroundService.class);
            startService(intent);
        });

        disable.setOnClickListener(v -> {
            Intent intent = new Intent(this, BackgroundService.class);
            stopService(intent);
        });
    }



    List<Address> addresses;
    private void sendSMS() {

        Geocoder geocoder=new Geocoder(this, Locale.getDefault());

        Log.e("latitude ",String.valueOf(latitude) );
        Log.e("longitude ", String.valueOf(longitude));


        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address =addresses.get(0).getAddressLine(0);
            String area =addresses.get(0).getLocality();
            String city =addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();
            String postalCode=addresses.get(0).getPostalCode();

            String fullAddress = address+", "+area+", "+city+", "+country+", "+postalCode;

            Log.e("fullAddress",fullAddress);
            MediaPlayer player = MediaPlayer.create(this,
                    Settings.System.DEFAULT_RINGTONE_URI);

            player.start();

            String message = "I am in DANGER,Save ME! \n I am at " +fullAddress+"\n";
            SharedPreferences sharedPreferences=getSharedPreferences("mypref", Context.MODE_PRIVATE);
            int size=sharedPreferences.getAll().size();
            if(size==0)
            {
                new AlertDialog.Builder(this)
                        .setTitle("")
                        .setMessage("No Emergency contacts!")

                        .setPositiveButton("Go to contacts", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), ContactsActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
            for(int i=1;i<=3;i++) {
                String c = "contact" + i;
                if (sharedPreferences.contains(c)) {
                    String details = sharedPreferences.getString(c, "");
                    if(details.length()>10)
                    {
                        String number =details.substring(details.length() - 10);
                        SmsManager mySmsManager = SmsManager.getDefault();
                        mySmsManager.sendTextMessage(number, null, message, null, null);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}