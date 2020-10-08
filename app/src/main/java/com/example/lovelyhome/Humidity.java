package com.example.lovelyhome;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Humidity extends AppCompatActivity {
    private int progress = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.humidity);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_security:
                        Intent intent1 = new Intent(Humidity.this, Security.class);
                        startActivity(intent1);
                        break;
                    case R.id.ic_humidity:
                        break;
                    case R.id.ic_temperature:
                        Intent intent3 = new Intent(Humidity.this, Temperature.class);
                        startActivity(intent3);
                        break;
                    case R.id.ic_light:
                        Intent intent4 = new Intent(Humidity.this, Light.class);
                        startActivity(intent4);
                        break;
                }
                return false;
            }
        });


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                getHumidityData();
            }
        },0,5000);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getHumidityData(){
        String hardwareName = "PI_05_";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        String referenceName = hardwareName + formatter.format(date);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(referenceName).child(getCurrentHour());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast toast = Toast.makeText(getApplicationContext(),"Refreshing",Toast.LENGTH_SHORT);
                    toast.show();
                    for(DataSnapshot child : snapshot.getChildren()){
                        String humidity = child.child("humid").getValue().toString();
                        ProgressBar progressBar = findViewById(R.id.humidity_circle);
                        double  humidity1 = Double.parseDouble(humidity);
                        int humidityCircle = (int)humidity1;
                        TextView textView = findViewById(R.id.textView);
                        textView.setText(humidity + " %");
                        progressBar.setProgress(humidityCircle);
                        setLCDText(humidity1);
                    }
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(),"Oops!!An Error Occurred!!Unable To Retrieve Data!!",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void setLCDText(Double humidity){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("PI_05_CONTROL");
        TextView textView = findViewById(R.id.textView2);
        View screenView = findViewById(R.id.humidity_background);
        myRef.child("lcd").setValue("1");
        if(humidity >= 80){
            //myRef.child("lcdtext").setValue("It might rain!!");
            textView.setText("It might rain! Please Bring an Umbrella When You Go Out!");
            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rain));
        } else if(humidity>=20 && humidity <=79){
            // myRef.child("lcdtext").setValue("It's cloudy day");
            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.back));
            textView.setText("It's cloudy day! Have A Nice Day!");
        }
        else{
            // myRef.child("lcdtext").setValue("It's sunny day!");
            textView.setText("It's sunny day! Please Pay Attention to Sun Protection When You Go Out!");
            screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sun));
        }
    }

    public String getCurrentHour(){
        Date date = new Date();
        SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");
        String currentHour = hourFormatter.format(date);
        return currentHour;
    }

}
