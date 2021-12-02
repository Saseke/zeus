package com.songmengyuan.zeus.common.config.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class LogAnalysisConfig {
    @SerializedName("kafka_server")
    private String kafkaServer;

    @SerializedName("kafka_port")
    private String kafkaPort;
    @SerializedName("topic_name")
    private String topicName;
}
