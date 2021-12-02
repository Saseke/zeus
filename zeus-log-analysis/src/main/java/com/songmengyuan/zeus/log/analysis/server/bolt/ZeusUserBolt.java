package com.songmengyuan.zeus.log.analysis.server.bolt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.model.ZeusUserAnalysisLog;

/**
 * 根据用户ip进行拆分
 */
public class ZeusUserBolt extends BaseBasicBolt {
    Map<String, ZeusUserAnalysisLog> userDataMap = new ConcurrentHashMap<>();

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        ZeusLog zeusLog = (ZeusLog)input.getValueByField("record");
        String key = zeusLog.getSourceHostIp();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(""));
    }
}
