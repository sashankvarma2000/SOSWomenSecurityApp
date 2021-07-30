package com.example.task2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ContactsActivity extends AppCompatActivity {


    TextView contact1,contact2,contact3;
    Button clear;
    SharedPreferences sharedPreferences;

    static final String myPref="mypref";

    String temp="000";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        clear = findViewById(R.id.clear);

        contact1 = findViewById(R.id.contact1);
        contact2 = findViewById(R.id.contact2);
        contact3 = findViewById(R.id.contact3);

        showDetails();

        clear.setOnClickListener(V->{

            sharedPreferences=getSharedPreferences(myPref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.clear();
            editor.apply();
            showDetails();
        });

        contact1.setOnClickListener(v-> {
            temp="100";
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        contact2.setOnClickListener(v -> {
            temp="010";
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        });
        contact3.setOnClickListener(v->{
            temp="001";
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = getContentResolver().query(contactData, null, null, null, null);

            if (c.moveToFirst()) {

                String phoneNumber = "";
                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));


                String hasPhoneNum = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (hasPhoneNum.equalsIgnoreCase("1"))
                    hasPhoneNum = "true";
                else
                    hasPhoneNum = "false";

                if (Boolean.parseBoolean(hasPhoneNum)) {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phones.close();
                }


                if(temp.equals("100"))
                {
                    sharedPreferences=getSharedPreferences(myPref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("contact1","contact1 :\n    Name: "+name+"\n    Number: "+phoneNumber);
                    editor.apply();
                }
                if(temp.equals("010"))
                {
                    sharedPreferences=getSharedPreferences(myPref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("contact2","contact2 :\n    Name: "+name+"\n    Number: "+phoneNumber);
                    editor.apply();
                }
                if(temp.equals("001"))
                {
                    sharedPreferences=getSharedPreferences(myPref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("contact3","contact3 :\n    Name: "+name+"\n    Number: "+phoneNumber);
                    editor.apply();
                }
            }
            showDetails();
            c.close();
        }
    }

    public void showDetails()
    {
        sharedPreferences=getSharedPreferences(myPref, Context.MODE_PRIVATE);
        for(int i=1;i<=3;i++)
        {
            String c="contact"+ i;
            if(sharedPreferences.contains(c)){
                String details= sharedPreferences.getString(c,"");
                if(i==1)
                {
                    contact1.setText(details);
                }
                else if(i==2)
                {
                    contact2.setText(details);
                }
                else
                {
                    contact3.setText(details);
                }
            }
            else
            {
                if(i==1)
                {
                    contact1.setText("");
                    contact1.setHint("contact1 :\tNot selected");
                }
                else if(i==2)
                {
                    contact2.setText("");
                    contact2.setHint("contact2 :\tNot selected");
                }
                else
                {
                    contact3.setText("");
                    contact3.setHint("contact3 :\tNot selected");
                }
            }

        }
    }

    public void clear(View view) {
        sharedPreferences=getSharedPreferences(myPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}