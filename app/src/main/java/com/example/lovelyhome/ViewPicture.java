package com.example.lovelyhome;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ViewPicture extends AppCompatActivity {

    private String storage_path = "PI_05_CONTROL/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_picture);

        final ProgressBar progressBar = findViewById(R.id.progressBar);

        final ArrayList<Image> imageList = new ArrayList<>();
        final RecyclerView recycleView  = findViewById(R.id.pictureRecycleView);
        final ImageAdapter adapter = new ImageAdapter(imageList,this);

        //set layout as linear layout
        recycleView.setLayoutManager(new LinearLayoutManager(this));

        StorageReference imgRef = FirebaseStorage.getInstance().getReference().child(storage_path);
        progressBar.setVisibility(View.VISIBLE);
        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

        imgRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {

                for (final StorageReference fileRef : listResult.getItems()){

                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            try {

                                Date past = format.parse(fileRef.getName().substring(4,18));
                                Date now = new Date();
                                Long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
                                //last 7 days records
                                if(days <= 7){
                                    Image image = new Image(fileRef.getName(),uri.toString());
                                    imageList.add(image);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            recycleView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }


}

