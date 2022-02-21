package com.songmengyuan.zeus.log.analysis.storm.server.bolt;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.google.gson.JsonSyntaxException;
import com.songmengyuan.zeus.common.config.constant.ZeusLogLevel;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.GsonUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusFilterBolt extends BaseBasicBolt {

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String rawMessage = input.getStringByField("value");
        if (!rawMessage.isEmpty()) {
            try {
                ZeusLog zeusLog = GsonUtil.getGson().fromJson(rawMessage, ZeusLog.class);
                if (zeusLog.getLevel().equals(ZeusLogLevel.RECORD) || zeusLog.getLevel().equals(ZeusLogLevel.TRAFFIC)) {
                    collector.emit(new Values(zeusLog.getLevel(), zeusLog));
                } else {
                    log.debug("the filtered data : {}", zeusLog);
                }
            } catch (JsonSyntaxException e) {
                log.error("type parse error,the message is: {}", rawMessage);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("level", "record"));
    }
}
