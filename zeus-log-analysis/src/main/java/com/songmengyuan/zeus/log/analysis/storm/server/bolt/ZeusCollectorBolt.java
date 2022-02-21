package com.songmengyuan.zeus.log.analysis.storm.server.bolt;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import com.songmengyuan.zeus.common.config.model.ZeusUserAnalysisLog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusCollectorBolt extends BaseBasicBolt {

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String token = input.getStringByField("token");
        ZeusUserAnalysisLog analysisLog = (ZeusUserAnalysisLog)input.getValueByField("log");
        log.info("[RECORD] {} user tokenId: {},ip address:{} visit website :{}", analysisLog.getDate(), token,
            analysisLog.getUserIpAddress(), analysisLog.getUrl());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

}
