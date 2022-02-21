package com.songmengyuan.zeus.common.config.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class LogAnalysisConfig {
    @SerializedName("kafka_server")
    private String kafkaServer;

    @SerializedName("kafka_port")
    private String kafkaPort;
    @SerializedName("zookeeper_server")
    private String zookeeperServer;
    @SerializedName("zookeeper_port")
    private String zookeeperPort;
    @SerializedName("topic_name")
    private String topicName;

    @SerializedName("calculate_method")
    private String calculateMethod;
}
