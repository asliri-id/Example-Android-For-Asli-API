package com.yusuffirdaus.aslirisalesdemo.api;


import android.app.Activity;
import android.content.res.Resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.provider.Settings.System.getString;
import com.yusuffirdaus.aslirisalesdemo.R;

public class RetroServer {

//your asiri api url
    private static final String baseUrl = "https://api.asliri.id:8443/internal/";
    private static Retrofit retro;

    public static Retrofit konekRetrofit() {

        if (retro == null) {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();


            retro = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

        }
        return retro;
    }

}
