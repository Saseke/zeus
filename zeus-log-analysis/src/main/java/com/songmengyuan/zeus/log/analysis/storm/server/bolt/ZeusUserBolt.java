package com.songmengyuan.zeus.log.analysis.storm.server.bolt;

import java.util.UUID;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.songmengyuan.zeus.common.config.constant.ZeusLogLevel;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.model.ZeusUserAnalysisLog;

import lombok.extern.slf4j.Slf4j;

/**
 * 根据用户ip进行拆分
 */
@Slf4j
public class ZeusUserBolt extends BaseBasicBolt {

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        try {
            ZeusLogLevel level = (ZeusLogLevel)input.getValueByField("level");
            ZeusLog zeusLog = (ZeusLog)input.getValueByField("record");
            if (level.equals(ZeusLogLevel.RECORD)) {
                ZeusUserAnalysisLog analysisLog = new ZeusUserAnalysisLog(UUID.randomUUID().toString(),
                    zeusLog.getUserId(), zeusLog.getSourceHostIp(), zeusLog.getDestHostName(), zeusLog.getTime());
                collector.emit(new Values(zeusLog.getUserId(), analysisLog));
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("token", "log"));
    }
}
