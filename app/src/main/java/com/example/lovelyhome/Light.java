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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Light extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light);

        retrieveDistance();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_security:
                        Intent intent1 = new Intent(Light.this, Security.class);
                        startActivity(intent1);
                        break;
                    case R.id.ic_humidity:
                        Intent intent2 = new Intent(Light.this, Humidity.class);
                        startActivity(intent2);
                        break;
                    case R.id.ic_temperature:
                        Intent intent3 = new Intent(Light.this, Temperature.class);
                        startActivity(intent3);
                        break;
                    case R.id.ic_light:
                        break;
                }
                return false;
            }
        });



    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void retrieveDistance() {
        final TextView txtDateUltraSonic = findViewById(R.id.txtDateUltraSonic);
        final View screenView = findViewById(R.id.light_background);
        final TextView txtNearby = findViewById(R.id.txtNearby);
        final TextView txtTurnOnOff = findViewById(R.id.txtTurnOnOff);
        final ProgressBar humidity_circle = findViewById(R.id.humidity_circle);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();

        Calendar now = Calendar.getInstance();



        String referenceName = "PI_05_" + formatter.format(date);

        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        String currentHourString = "";
        if(currentHour < 10){
            currentHourString = "0" + Integer.toString(currentHour);
        }
        else {
            currentHourString = Integer.toString(currentHour);

        }
        Log.d("CheckHere",String.valueOf(currentHour));

        txtDateUltraSonic.setText(formatter.format(date));


        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim);

        humidity_circle.startAnimation(animation);

        DatabaseReference refDistance = FirebaseDatabase.getInstance().getReference(referenceName).child(currentHourString);
        refDistance.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    for (DataSnapshot child : snapshot.getChildren()) {

                        String distance = child.child("ultra").getValue().toString();
                        double distanceInt = Double.parseDouble(distance);


                        if(distanceInt < 50.0){
                            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rain));
                            txtNearby.setText("Distance nearby is " + distanceInt + "cm");
                            txtTurnOnOff.setText("Turn on the light");
                            Log.d("CheckHere",String.valueOf(distanceInt));

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("PI_05_CONTROL");
                            myRef.child("led").setValue("1");


                        }
                        else{
                            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.back));
                            txtNearby.setText("Distance nearby is " + distanceInt + "cm");
                            txtTurnOnOff.setText(" Turn off the light");

                            Log.d("CheckHere0",String.valueOf(distanceInt));

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("PI_05_CONTROL");
                            myRef.child("led").setValue("0");


                        }

                    }
                }
                //Progress dialog method
                final ProgressDialog progress = new ProgressDialog(Light.this);

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


                Toast toast = Toast.makeText(getApplicationContext(),"Refresh now",Toast.LENGTH_SHORT);
                toast.show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}