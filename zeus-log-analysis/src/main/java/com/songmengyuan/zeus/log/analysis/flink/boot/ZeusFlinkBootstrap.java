package com.songmengyuan.zeus.log.analysis.flink.boot;

import java.util.Properties;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import com.songmengyuan.zeus.common.config.config.LogAnalysisConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusFlinkBootstrap {
    public static void start(LogAnalysisConfig config) {
        try {
            // set up the streaming execution environment
            final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
            // 默认情况下，检查点被禁用。要启用检查点，请在StreamExecutionEnvironment上调用enableCheckpointing(n)方法，
            // 其中n是以毫秒为单位的检查点间隔。每隔5000 ms进行启动一个检查点,则下一个检查点将在上一个检查点完成后5秒钟内启动
            env.enableCheckpointing(5000);
            env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
            Properties properties = new Properties();

            properties.setProperty("bootstrap.servers", "172.16.159.137:9092");// kafka的节点的IP或者hostName，多个使用逗号分隔
            properties.setProperty("zookeeper.connect", "172.16.159.137:2181");// zookeeper的节点的IP或者hostName，多个使用逗号进行分隔
            properties.setProperty("group.id", "test-consumer-group");// flink consumer flink的消费者的group.id
            FlinkKafkaConsumer<String> myConsumer =
                new FlinkKafkaConsumer<String>("zeus", new SimpleStringSchema(), properties);// test0是kafka中开启的topic
            myConsumer.setStartFromEarliest();
            DataStream<String> stream = env.addSource(myConsumer);

            env.enableCheckpointing(5000);
            stream.print();
            env.execute("Flink Streaming Java API Skeleton");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
