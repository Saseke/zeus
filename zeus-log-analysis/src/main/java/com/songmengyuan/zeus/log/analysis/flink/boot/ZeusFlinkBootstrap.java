package com.songmengyuan.zeus.log.analysis.flink.boot;

import com.alibaba.fastjson.JSONObject;
import com.songmengyuan.zeus.common.config.constant.ZeusLogLevel;
import com.songmengyuan.zeus.common.config.model.ZeusFlinkUserAnalysisLog;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.model.ZeusUserAnalysis;
import com.songmengyuan.zeus.common.config.util.GsonUtil;
import com.songmengyuan.zeus.log.analysis.flink.service.UserAnalyzer;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.songmengyuan.zeus.common.config.config.LogAnalysisConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

@Slf4j
public class ZeusFlinkBootstrap {
    public static void start(LogAnalysisConfig config) {
        try {
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            KafkaSource<String> source =
                KafkaSource.<String>builder().setBootstrapServers(config.getKafkaServer() + ":" + config.getKafkaPort())
                    .setTopics("zeus").setGroupId("zeus-group").setStartingOffsets(OffsetsInitializer.earliest())
                    .setValueOnlyDeserializer(new SimpleStringSchema()).build();
            // read data from kafka.
            DataStream<String> kafka_source = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
            // deserialization
            DataStream<ZeusLog> zeusLogDataStream =
                kafka_source.map(e -> GsonUtil.getGson().fromJson(e, ZeusLog.class));
            DataStream<ZeusFlinkUserAnalysisLog> userLogsStream =
                zeusLogDataStream.filter(e -> ZeusLogLevel.TRAFFIC.equals(e.getLevel()))
                    .map(new UserAnalyzer.ZeusLogMapFunction()).assignTimestampsAndWatermarks(
                        WatermarkStrategy.<ZeusFlinkUserAnalysisLog>forBoundedOutOfOrderness(Duration.ofMillis(200))
                            .withTimestampAssigner(new UserAnalyzer.ZeusLogTimestampAssigner()));
            // grouped by user ID and site ip.
            DataStream<ZeusUserAnalysis> windowStream = userLogsStream.keyBy(new UserAnalyzer.UserLogKeySelector())
                .window(SlidingEventTimeWindows.of(Time.seconds(5), Time.milliseconds(500)))
                .aggregate(new UserAnalyzer.UserAggregateFunction(), new UserAnalyzer.UserProcessWindowFunction());
            windowStream.print();
            // SingleOutputStreamOperator<String> process = windowStream.keyBy(new
            // UserAnalyzer.UserLogWindowKeySelector())
            // .process(new UserAnalyzer.TopHotSiteFunction());
            // process.print();
            env.execute("zeus of log analysis.");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
