package com.example.lovelyhome;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Security extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private int sound;
    private double ultrasonic;
    private Button btnTurnOnBuzzer;
    private Button btnTurnOffBuzzer;
    private TextView textView2;
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.security);
        ActivityCompat.requestPermissions(Security.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        //bottom navigation
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_security:
                        break;
                    case R.id.ic_humidity:
                        Intent intent2 = new Intent(Security.this, Humidity.class);
                        startActivity(intent2);
                        break;
                    case R.id.ic_temperature:
                        Intent intent3 = new Intent(Security.this, Temperature.class);
                        startActivity(intent3);
                        break;
                    case R.id.ic_light:
                        Intent intent4 = new Intent(Security.this, Light.class);
                        startActivity(intent4);
                        break;
                }
                return false;
            }
        });

        btnTurnOnBuzzer = findViewById(R.id.btnTurnOnAlarm);
        btnTurnOffBuzzer = findViewById(R.id.btnTurnOffAlarm);
        textView2 = findViewById(R.id.textView2);

        btnTurnOffBuzzer.setEnabled(false);

        btnTurnOnBuzzer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                manageBuzzer("1");
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Successful turned On",
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        btnTurnOffBuzzer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                manageBuzzer("0");
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Successful turned off",
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        Button btnHistory = findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    openActivity();
            }
        });
        getData();
    }

    public void openActivity(){
        Intent intent = new Intent(Security.this, ViewPicture.class);
        startActivity(intent);

    }
    public void getData(){

        String raspberryPiSetName  = "PI_05_";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        final String referenceName = raspberryPiSetName + formatter.format(date); //eg: PI_05_20200815

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(referenceName).child(getCurrentHour());
        myRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    for(DataSnapshot child : snapshot.getChildren()){


                        //currentDateTime.setText(referenceName + " " +getCurrentHour() + " " + getCurrentMinuteSecond()); //display retrieving time

                         sound = Integer.parseInt(child.child("sound").getValue().toString());

                         ultrasonic = Double.parseDouble(child.child("ultra2").getValue().toString());

                        displayCurrentSituation();
                    }
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(),"Stopped retrieving data...",Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void displayCurrentSituation(){
        final TextView data = findViewById(R.id.txtResult);
        final TextView currentSituation= findViewById(R.id.txtCurrentSituation);

        String currentSituationMessage = "";
        int count = 0;
        if (sound >= 300){
            currentSituationMessage += "It's noisy outside.\n";
            count = 1;
        }
        if(ultrasonic <= 30){
            currentSituationMessage += "Someone passing outside.";
            count = 1;
        }

        if(count == 1){

            doProtect();

        }else{
            currentSituationMessage = "Outside is peaceful now.";

        }
        currentSituation.setText(currentSituationMessage);
        data.setText("Sound: " + sound + "\nUltrasonic: " + ultrasonic);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void doProtect(){
        TextView notification = findViewById(R.id.txtNotification);
        notification.getResources().getColor(R.color.securityTextColor);
        Date date = new Date();
        SimpleDateFormat dateTime = new SimpleDateFormat("dd-MMM-YYYY hh:mm aa");
        String notificationDateTime = dateTime.format(date);

        String message = notificationDateTime + "\n";

        if (sound >= 300){
            message += "Strange sound outside your house just now.\n";
        }

        if (ultrasonic <= 30){

            //if someone is near less than 30cm

            //remove the comments for the taking picture and change lcd text
            /*setLCDText("Please go away!");
            takePicture("1");*/
            message += "Someone were near your main gate in " + ultrasonic + "cm.\n";
            message += "A picture has taken.";

        }else{

            takePicture("0");
            setLCDText("=App is running=");
        }
        SpannableString content = new SpannableString(message);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        content.setSpan(boldSpan,0,20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        notification.setText(content);
    }

    //Turn on/off buzzer
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void manageBuzzer(String active){
        DatabaseReference myRef = database.getReference("PI_05_CONTROL");
        myRef.child("buzzer").setValue(active);

        if(active == "1"){
            btnTurnOffBuzzer.setEnabled(true);
            textView2.setText(R.string.AlarmIsOn);
            textView2.setTextColor(Color.rgb(255,0,0));
        }else{
            btnTurnOffBuzzer.setEnabled(false);
            textView2.setText(R.string.AlarmIsOff);
            textView2.setTextColor(Color.rgb(0,0,255));
        }
    }

    //Take picture
    public void takePicture(String active){
        DatabaseReference myRef = database.getReference("PI_05_CONTROL");
        myRef.child("camera").setValue(active);
    }

    //Set LCD text
    public void setLCDText(String text){
        //original =App is running=
        DatabaseReference myRef = database.getReference("PI_05_CONTROL");
        myRef.child("lcdtext").setValue(text);
    }

    public String getCurrentHour (){
        Date date = new Date();
        SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");
        String currentHour = hourFormatter.format(date);
        return currentHour;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getCurrentMinuteSecond (){
        Date date = new Date();
        SimpleDateFormat minuteFormatter = new SimpleDateFormat("mm");
        String currentMinute = minuteFormatter.format(date);
        int second = LocalDateTime.now().getSecond();
        String currentSecond;
        if(second / 10 >= 1 && second / 10 <= 10 ){
            currentSecond = second/10 + "0"; //25 divide 10 = 2 , 2 + "0" = 20
        }else {
            currentSecond = "00"; // 5 divide 10 = 0
        }
        return currentMinute + "" +currentSecond; //+ currentSecond;
    }
}
