package ru.practicum.tracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.tracker.model.adapter.DurationAdapter;
import ru.practicum.tracker.model.adapter.LocalDateTimeAdapter;

import java.time.Duration;
import java.time.LocalDateTime;

public class GsonUtils {
    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }
}