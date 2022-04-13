package com.songmengyuan.zeus.log.analysis.flink.boot;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.songmengyuan.zeus.common.config.config.LogAnalysisConfig;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;
import com.songmengyuan.zeus.log.analysis.flink.service.UserAnalyzer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusFlinkBootstrap {
    public static void start(LogAnalysisConfig config) {
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
             env.setParallelism(1);
            env.enableCheckpointing(2000);
            env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
            env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
            env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000);
            env.getCheckpointConfig().setCheckpointTimeout(3000);
            env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
            KafkaSource<String> source =
                KafkaSource.<String>builder().setBootstrapServers(config.getKafkaServer() + ":" + config.getKafkaPort())
                    .setTopics("zeus").setGroupId("zeus-group").setStartingOffsets(OffsetsInitializer.latest())
                    .setValueOnlyDeserializer(new SimpleStringSchema()).build();
            DataStream<String> kafka_source = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
            DataStream<ZeusLog> zeusLogDataStream =
                kafka_source.map(e -> GsonUtil.getGson().fromJson(e, ZeusLog.class));
            DataStream<String> resultStream = UserAnalyzer.selectHotSize(zeusLogDataStream);
            resultStream.print();
            env.execute("zeus of log analysis.");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
