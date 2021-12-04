package com.songmengyuan.zeus.log.analysis.server.boot;

import com.songmengyuan.zeus.log.analysis.server.bolt.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.KafkaSpoutRetryExponentialBackoff;
import org.apache.storm.kafka.spout.KafkaSpoutRetryService;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import com.songmengyuan.zeus.common.config.config.LogAnalysisConfig;
import com.songmengyuan.zeus.common.config.config.LogAnalysisConfigLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusLogAnalysisBootstrap {

    public static void start(String configPath, String[] args) throws Exception {
        final LogAnalysisConfig config = LogAnalysisConfigLoader.load(configPath);
        log.info("load {} config file success", configPath);
        log.info("the config file information :{}", config);
        final TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka_spout", new KafkaSpout<>(
            getKafkaSpoutConfig(config.getKafkaServer() + ":" + config.getKafkaPort(), config.getTopicName())), 1);
        builder.setBolt("zeus_filter_bolt", new ZeusFilterBolt()).shuffleGrouping("kafka_spout");
        builder.setBolt("zeus_user_bolt", new ZeusUserBolt()).shuffleGrouping("zeus_filter_bolt");
        builder.setBolt("zeus_traffic_bolt", new ZeusTrafficBolt()).shuffleGrouping("zeus_filter_bolt");
        builder.setBolt("zeus_collector_bolt", new ZeusCollectorBolt()).fieldsGrouping("zeus_user_bolt",
            new Fields("token"));
        builder.setBolt("zeus_traffic_connector_bolt", new ZeusTrafficCollector()).fieldsGrouping("zeus_traffic_bolt",
            new Fields("token"));
        // 如果外部传参 cluster 则代表线上环境启动,否则代表本地启动
        if (args.length > 0 && args[0].equals("cluster")) {
            StormSubmitter.submitTopology("ClusterReadingFromKafkaApp", new Config(), builder.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("LocalReadingFromKafkaApp", new Config(), builder.createTopology());
        }
    }

    private static KafkaSpoutConfig<String, String> getKafkaSpoutConfig(String bootstrapServers, String topic) {
        return KafkaSpoutConfig.builder(bootstrapServers, topic)
            // 除了分组 ID,以下配置都是可选的。分组 ID 必须指定,否则会抛出 InvalidGroupIdException 异常
            .setProp(ConsumerConfig.GROUP_ID_CONFIG, "kafkaSpoutTestGroup")
            // 定义重试策略
            .setRetry(getRetryService())
            // 定时提交偏移量的时间间隔,默认是 15s
            .setOffsetCommitPeriodMs(10_000).build();
    }

    // 定义重试策略
    private static KafkaSpoutRetryService getRetryService() {
        return new KafkaSpoutRetryExponentialBackoff(KafkaSpoutRetryExponentialBackoff.TimeInterval.microSeconds(500),
            KafkaSpoutRetryExponentialBackoff.TimeInterval.milliSeconds(2), Integer.MAX_VALUE,
            KafkaSpoutRetryExponentialBackoff.TimeInterval.seconds(10));
    }
}
