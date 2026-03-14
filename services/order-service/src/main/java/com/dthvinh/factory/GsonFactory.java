package com.dthvinh.factory;

import java.time.Instant;

import com.dthvinh.utils.InstantAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonFactory {
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }
}
