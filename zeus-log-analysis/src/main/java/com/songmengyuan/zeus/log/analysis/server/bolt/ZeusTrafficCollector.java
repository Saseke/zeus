package com.songmengyuan.zeus.log.analysis.server.bolt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import com.songmengyuan.zeus.common.config.util.TrafficUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusTrafficCollector extends BaseBasicBolt {
    private final Map<String, Long> trafficMap = new ConcurrentHashMap<>();

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String token = input.getStringByField("token");
        String ip = input.getStringByField("ip");
        Integer traffic = input.getIntegerByField("traffic");
        if (!trafficMap.containsKey(token)) {
            trafficMap.put(token, Long.valueOf(traffic));
        } else {
            trafficMap.put(token, trafficMap.get(token) + traffic);
        }
        log.info("[TRAFFIC] token :{},ip: {} spends {} traffic.", token, ip,
            TrafficUtil.calculateTraffic(trafficMap.get(token)));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
