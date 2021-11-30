package com.songmengyuan.zeus.common.config.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LogAnalysisConfigLoader {
    public static LogAnalysisConfig load(String file) throws Exception {
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(file)) {
            JsonReader reader;
            reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            LogAnalysisConfig config = new Gson().fromJson(reader, LogAnalysisConfig.class);
            reader.close();
            return config;
        }
    }
}
