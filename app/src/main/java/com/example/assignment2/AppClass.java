package com.example.assignment2;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

public class AppClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // In your Application class or initialization point
        Glide.with(this).setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888));

    }
}
