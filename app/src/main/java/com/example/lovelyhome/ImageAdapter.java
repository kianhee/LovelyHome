package com.example.lovelyhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{
    private ArrayList<Image> imageList;
    private Context context;

    private OutputStream outputStream = null;


    public ImageAdapter(ArrayList<Image> imageList, Context context){
        this.imageList = imageList;
        this.context = context;

    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_picture,parent,false);
        return new ViewHolder(view);
    }


    private int count = 1;
    @Override
    public void onBindViewHolder(final ImageAdapter.ViewHolder holder, final int position) {


        //sample cam_20200813164800.jpg
        holder.txtFileName.setText(imageList.get(getItemCount()-1-position).getFileName());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");

        try {

            Date past = format.parse(imageList.get(getItemCount()-1-position).getFileName().substring(4,18));
            Date now = new Date();
            Long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            Long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            Long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            Long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
            String dateTime = format2.format(past);

            if(seconds < 60){

                holder.txtDateTime.setText(dateTime + "  " + seconds + " seconds ago");

            }else if(minutes < 60){

                holder.txtDateTime.setText(dateTime + "  " + minutes + " minutes ago");

            }else if(hours < 24){

                holder.txtDateTime.setText(dateTime + "  " + hours + " hours ago");

            }else{

                holder.txtDateTime.setText(dateTime + "  " + days + " days ago");

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        Picasso.get().load(imageList.get(getItemCount()-1-position).getImage()).into(holder.imageView);


        holder.btnSave.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {


                BitmapDrawable drawable = (BitmapDrawable) holder.imageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                File filePath = Environment.getExternalStorageDirectory();
                File dir = new File(filePath.getAbsoluteFile() + "/Images/");

                dir.mkdir();

                String fileName = imageList.get(getItemCount()-1-position).getFileName();

                File file = new File(dir,fileName);
                try{

                    outputStream = new FileOutputStream(file);
                    System.out.println(file + " " + outputStream);

                }catch (FileNotFoundException e){

                    e.printStackTrace();
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                Toast toast = Toast.makeText(context,
                        fileName + " successful saved into gallery LovelyHome",
                        Toast.LENGTH_SHORT);
                toast.show();

                try{
                    outputStream.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
                try{
                    outputStream.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public int getItemCount(){
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView txtDateTime;
        TextView txtFileName;
        Button btnSave;

        public ViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPicture);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            txtFileName = itemView.findViewById(R.id.txtFileName);
            btnSave = itemView.findViewById(R.id.btnSave);

        }
    }

}
