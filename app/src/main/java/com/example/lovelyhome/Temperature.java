package com.example.lovelyhome;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

public class Temperature extends AppCompatActivity {

    //ProgressDialog progressDialog;
    private Button BtnOn;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temperature);

        TextView txtTemp = findViewById(R.id.txtTemp);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_security:
                        Intent intent1 = new Intent(Temperature.this, Security.class);
                        startActivity(intent1);
                        break;
                    case R.id.ic_humidity:
                        Intent intent2 = new Intent(Temperature.this, Humidity.class);
                        startActivity(intent2);
                        break;
                    case R.id.ic_temperature:
                        break;
                    case R.id.ic_light:
                        Intent intent4 = new Intent(Temperature.this, Light.class);
                        startActivity(intent4);
                        break;
                }
                return false;
            }
        });

        //retrieveTemperature();

        BtnOn = findViewById(R.id.BtnOn);
        BtnOn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                if(BtnOn.getText() == "Turn OFF") {
                    Toast toast = Toast.makeText(getApplicationContext(), "You turn on the temperature.", Toast.LENGTH_SHORT);
                    toast.show();

                    BtnOn.setText("Turn on");
                    BackDefault();
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(), "You turn off the temperature.", Toast.LENGTH_SHORT);
                    toast.show();
                    retrieveTemperature();
                    BtnOn.setText("Turn OFF");
                }
            }

        });
    }

    private void BackDefault() {
        TextView txtDate = findViewById(R.id.txtDate);
        TextView txtHour = findViewById(R.id.txtHour);
        TextView txtMinute = findViewById(R.id.txtMinute);
        final TextView txtReminder = findViewById(R.id.txtReminder);
        final TextView txtTemp = findViewById(R.id.txtTemp);

        txtTemp.setText("Temperature 'C");
        txtReminder.setText("You off the sensor, Press the button to turn on");

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void retrieveTemperature() {

        TextView txtDate = findViewById(R.id.txtDate);
        TextView txtHour = findViewById(R.id.txtHour);
        TextView txtMinute = findViewById(R.id.txtMinute);
        final TextView txtReminder = findViewById(R.id.txtReminder);
        final TextView txtTemp = findViewById(R.id.txtTemp);
        BtnOn = findViewById(R.id.BtnOn);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim);

        txtTemp.startAnimation(animation);
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;

        String formattedDate = formatter.format(LocalDate.now());

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        int currentMinute = cal.get(Calendar.MINUTE);

        txtDate.setText(formattedDate);
        txtHour.setText(Integer.toString(currentHour));
        if (currentHour < 10) {
            txtHour.setText("0" + Integer.toString(currentHour));
        }  else {
            txtHour.setText(Integer.toString(currentHour));
        }
        txtMinute.setText(txtHour.getText().toString() + String.valueOf(Integer.toString(currentMinute)));

        Log.d("CheckHere", txtHour.getText().toString());

        String referenceName = "PI_05_" + txtDate.getText().toString().trim();
        //String referenceName = "PI_05_20200904";
        DatabaseReference refTemp = FirebaseDatabase.getInstance().getReference(referenceName).child(txtHour.getText().toString().trim());
        //DatabaseReference refTemp = FirebaseDatabase.getInstance().getReference(referenceName).child("00");

        refTemp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && BtnOn.getText() == "Turn OFF") {

                    for (DataSnapshot child : snapshot.getChildren()) {
                        String temperature = child.child("tempe").getValue().toString();
                        //String temperature = "18";


                        if (Double.parseDouble(temperature) >= 30.0) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("PI_05_CONTROL");
                            myRef.child("led").setValue("2");

                            txtTemp.setText(temperature + "°C");
                            txtReminder.setText("Temperature high, turning on the fan, Level 2");

                        }
                        else if (Double.parseDouble(temperature) >= 20.0) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("PI_05_CONTROL");
                            myRef.child("led").setValue("1");

                            txtTemp.setText(temperature + "°C");
                            txtReminder.setText("Temperature acceptable.turning on the fan, Level 1");
                        }  else {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("PI_05_CONTROL");
                            myRef.child("led").setValue("0");

                            txtTemp.setText(temperature + "°C");
                            txtReminder.setText("Temperature lower, turning off the fan, Level 0");
                        }

                    }
                    //Progress dialog method
                    final ProgressDialog progress = new ProgressDialog(Temperature.this);

                    progress.show();
                    progress.setContentView(R.layout.progress_dialog);

                    progress.getWindow().setBackgroundDrawableResource(

                            android.R.color.transparent
                    );


                    Runnable progressRunnable = new Runnable() {

                        @Override
                        public void run() {
                            progress.cancel();
                        }
                    };

                    Handler pdCanceller = new Handler();
                    pdCanceller.postDelayed(progressRunnable, 2000);
                    //Toast method
                    //Toast toast = Toast.makeText(getApplicationContext(),"Refresh new temperature now",Toast.LENGTH_SHORT);
                    // toast.show();
                } else {
                    // Toast toast = Toast.makeText(getApplicationContext(), "Dont have the database data", Toast.LENGTH_SHORT);
                    // toast.show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}