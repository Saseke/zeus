package com.songmengyuan.zeus.log.analysis.server.bolt;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusCollectorBolt extends BaseBasicBolt {

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        ZeusLog zeusLog = (ZeusLog)input.getValueByField("record");
        log.info(GsonUtil.getGson().toJson(zeusLog));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

}
