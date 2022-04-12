package com.songmengyuan.zeus.log.analysis.flink.service;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import com.google.common.util.concurrent.AtomicDouble;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.util.TrafficUtil;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class TrafficAnalyzer extends KeyedProcessFunction<String, ZeusLog, String> {
    private final AtomicDouble atomicDouble = new AtomicDouble();

    @Override
    public void processElement(ZeusLog zeusLog, Context context, Collector<String> collector) throws Exception {
        // String token = input.getStringByField("token");
        // String ip = input.getStringByField("ip");
        // Integer traffic = input.getIntegerByField("traffic");
        // if (!trafficMap.containsKey(token)) {
        // trafficMap.put(token, Long.valueOf(traffic));
        // } else {
        // trafficMap.put(token, trafficMap.get(token) + traffic);
        // }
        atomicDouble.getAndAdd(zeusLog.getTraffic());
        String res = String.format("time:%s [TRAFFIC] token: %s,ip: %s spends %s traffic.", zeusLog.getTime(),
            zeusLog.getUserId(), zeusLog.getSourceHostIp(), TrafficUtil.calculateTraffic(atomicDouble.get()));
        log.info(res);
        collector.collect(res);
    }
}
