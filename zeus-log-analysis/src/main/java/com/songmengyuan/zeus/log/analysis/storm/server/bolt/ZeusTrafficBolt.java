package com.songmengyuan.zeus.log.analysis.storm.server.bolt;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.songmengyuan.zeus.common.config.constant.ZeusLogLevel;
import com.songmengyuan.zeus.common.config.model.ZeusLog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusTrafficBolt extends BaseBasicBolt {
    // private final Map<String, Integer> trafficMap = new ConcurrentHashMap<>();

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        try {
            ZeusLogLevel level = (ZeusLogLevel)input.getValueByField("level");
            if (level.equals(ZeusLogLevel.TRAFFIC)) {
                ZeusLog zeusLog = (ZeusLog)input.getValueByField("record");
                collector.emit(new Values(zeusLog.getUserId(), zeusLog.getSourceHostIp(), zeusLog.getTraffic()));
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("token", "ip", "traffic"));
    }
}
