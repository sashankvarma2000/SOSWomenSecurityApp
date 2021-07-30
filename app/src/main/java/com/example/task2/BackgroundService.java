package com.example.task2;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class BackgroundService extends Service implements SensorEventListener {

    public static final String CHANNEL_ID="backGroundServiceChannel";


    SmsManager mySmsManager;
    Geocoder geocoder;
    List<Address> addresses;
    double latitude=17.3850,longitude=78.4867;

    //sensor variables
    private Sensor accelerometerSensor;
    private boolean isAccelerometerSensorAvailable,isFirst=true;
    private SensorManager sensorManager;
    private float currentX,currentY,currentZ,lastX,lastY,lastZ;
    private float xDiff,yDiff,zDiff;
    private float shakeThreshold=5;
    private Vibrator vibrator;
    private float lastTime=0,presentTime=0;




    public static class BackgroundBinder extends Binder{
        BackgroundBinder getService(){
            return this;
        }
    }

    //constructor
    public BackgroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        sendSMS();
        return smsBinder;
    }


    @Override
    public void onCreate() {
        mySmsManager=SmsManager.getDefault();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        registerReceiver(receiver, filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if(isAccelerometerSensorAvailable)
            sensorManager.unregisterListener(this);
    }


    private String fullAddress="";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);


        vibrator =(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);

        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null)
        {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            isAccelerometerSensorAvailable=true;
        }
        else{

            isAccelerometerSensorAvailable=false;
        }
        if(isAccelerometerSensorAvailable)
            sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);

        Intent notificationIntent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,
                0,notificationIntent,0);

        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Emergency")
                .setContentText("Message will be sent")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);

        return START_STICKY;
    }



    private final IBinder smsBinder = new BackgroundService.BackgroundBinder();


    private void sendSMS() {



        GpsTracker gpsTracker=new GpsTracker(getApplicationContext());
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();
        geocoder=new Geocoder(this, Locale.getDefault());

        Log.e("latitude ",String.valueOf(latitude) );
        Log.e("longitude ", String.valueOf(longitude));


        try {
            addresses = geocoder.getFromLocation(latitude,longitude,1);
            String address =addresses.get(0).getAddressLine(0);
            String area =addresses.get(0).getLocality();
            String city =addresses.get(0).getAdminArea();
            String country=addresses.get(0).getCountryName();
            String postalCode=addresses.get(0).getPostalCode();

            fullAddress = address+", "+area+", "+city+", "+country+", "+postalCode;

            Log.e("fullAddress",fullAddress);
            MediaPlayer player = MediaPlayer.create(this,
                    Settings.System.DEFAULT_RINGTONE_URI);
            //staring the player
            player.start();

            String message = "I am in danger,Help ME! \n I am at " +fullAddress+"\n";
            SharedPreferences sharedPreferences=getSharedPreferences("mypref", Context.MODE_PRIVATE);
            int size=sharedPreferences.getAll().size();
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

    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }




    long count=0,startTime=0,endTime=0;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(count==0)
            {
                startTime=System.currentTimeMillis();
            }
            if(action.equals(Intent.ACTION_SCREEN_OFF)){
                count++;
            }
            else if(action.equals(Intent.ACTION_SCREEN_ON)){
                count++;
            }
            if(count>=4)
            {
                endTime=System.currentTimeMillis();
                if(endTime-startTime<5000)
                {
                    Log.e("Power button pressed 4 times : ",": sms sent");
                    sendSMS();
                    count=0;
                }
            }

            if(System.currentTimeMillis()-startTime>5000)
            {
                startTime=System.currentTimeMillis();
                count=1;
            }
            Log.e("count",String.valueOf(count) );

        }
    };


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    @Override
    public void onSensorChanged(SensorEvent event) {
        currentX=event.values[0];
        currentY=event.values[1];
        currentZ=event.values[2];
        if(!isFirst)
        {
            xDiff = Math.abs(lastX-currentX);
            yDiff = Math.abs(lastY-currentY);
            zDiff = Math.abs(lastZ-currentZ);

            if((xDiff>shakeThreshold&&yDiff>shakeThreshold)||
                    (xDiff>shakeThreshold&&zDiff>shakeThreshold)||
                    (zDiff>shakeThreshold&&yDiff>shakeThreshold))
            {
                Log.e("ShakeDetected",": sms sent");
                sendSMS();
            }

        }

        lastX=currentX;
        lastY=currentY;
        lastZ=currentZ;
        isFirst=false;
    }
}
