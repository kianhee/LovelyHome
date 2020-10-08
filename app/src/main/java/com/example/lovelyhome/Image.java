package com.example.lovelyhome;

public class Image {

    private String fileName;
    private String image;

    public Image(){

    }

    public Image(String dateTime, String image){
        this.fileName = dateTime;
        this.image = image;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String dateTime) {
        this.fileName = dateTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
