package com.songmengyuan.zeus.common.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigLoader {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConfigLoader.class);

    public static Config load(String file) throws Exception {
        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(file)) {
            JsonReader reader;
            reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            Config config = new Gson().fromJson(reader, Config.class);
            reader.close();
            return config;
        }
    }

}
