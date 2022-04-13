package com.songmengyuan.zeus.log.analysis.flink.service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;

import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import com.songmengyuan.zeus.common.config.constant.ZeusLogLevel;
import com.songmengyuan.zeus.common.config.model.ZeusFlinkUserAnalysisLog;
import com.songmengyuan.zeus.common.config.model.ZeusLog;
import com.songmengyuan.zeus.common.config.model.ZeusUserAnalysis;

public class UserAnalyzer {
    // 获取用户一小时内访问网站的前三名网站地址.5s中更新一次
    public static DataStream<String> selectHotSize(DataStream<ZeusLog> zeusLogDataStream) {
        DataStream<ZeusFlinkUserAnalysisLog> userLogsStream =
            zeusLogDataStream.filter(e -> ZeusLogLevel.TRAFFIC.equals(e.getLevel())).map(new ZeusLogMapFunction())
                .assignTimestampsAndWatermarks(
                    WatermarkStrategy.<ZeusFlinkUserAnalysisLog>forBoundedOutOfOrderness(Duration.ofMillis(200))
                        .withTimestampAssigner(new ZeusLogTimestampAssigner()));
        DataStream<ZeusUserAnalysis> windowStream = userLogsStream.keyBy(new UserLogKeySelector())
            .window(SlidingEventTimeWindows.of(Time.seconds(20), Time.milliseconds(500)))
            .aggregate(new UserAggregateFunction(), new UserProcessWindowFunction());
        return windowStream.keyBy(new UserLogWindowKeySelector()).process(new TopHotSiteFunction(3));
    }

    public static class TopHotSiteFunction extends KeyedProcessFunction<Long, ZeusUserAnalysis, String> {
        private ListState<ZeusUserAnalysis> userAnalysisListState;
        private Integer topN;

        public TopHotSiteFunction(Integer topN) {
            this.topN = topN;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            userAnalysisListState =
                getRuntimeContext().getListState(new ListStateDescriptor<>("user-analysis", ZeusUserAnalysis.class));
        }

        @Override
        public void close() throws Exception {
            userAnalysisListState.clear();
        }

        @Override
        public void processElement(ZeusUserAnalysis value,
            KeyedProcessFunction<Long, ZeusUserAnalysis, String>.Context ctx, Collector<String> out) throws Exception {
            userAnalysisListState.add(value);
            ctx.timerService().registerEventTimeTimer(value.getWindowEnd() + 1);
        }

        @Override
        public void onTimer(long timestamp, KeyedProcessFunction<Long, ZeusUserAnalysis, String>.OnTimerContext ctx,
            Collector<String> out) throws Exception {
            StringBuilder sb = new StringBuilder();
            ArrayList<ZeusUserAnalysis> zeusUserAnalyses = Lists.newArrayList(userAnalysisListState.get().iterator());
            zeusUserAnalyses.sort((o1, o2) -> (int)(o2.getCount() - o1.getCount()));
            for (int i = 0; i < Math.min(topN, zeusUserAnalyses.size()); i++) {
                ZeusUserAnalysis log = zeusUserAnalyses.get(i);
                String result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(log.getWindowEnd());
                sb.append("cur Date: ").append(result).append("  ").append("NO").append(i + 1).append("  ")
                    .append("userId ").append(log.getUserId()).append(" ").append("ip地址: ").append(log.getUserIpAddress())
                    .append(" ").append("访问网站ip地址: ").append(log.getDestHostIp()).append("  ").append("访问次数 ")
                    .append(log.getCount()).append(System.lineSeparator());
            }
            out.collect(sb.toString());
        }
    }

    public static class UserProcessWindowFunction
        implements WindowFunction<Long, ZeusUserAnalysis, Tuple3<String, String, String>, TimeWindow> {

        @Override
        public void apply(Tuple3<String, String, String> tuple, TimeWindow window, Iterable<Long> input,
            Collector<ZeusUserAnalysis> out) {
            out.collect(new ZeusUserAnalysis(tuple.f0, tuple.f1, window.getEnd(), tuple.f2, input.iterator().next()));
        }
    }

    // calculate the tmp value.
    public static class UserAggregateFunction implements AggregateFunction<ZeusFlinkUserAnalysisLog, Long, Long> {

        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(ZeusFlinkUserAnalysisLog zeusFlinkUserAnalysisLog, Long aLong) {
            return aLong + 1;
        }

        @Override
        public Long getResult(Long aLong) {
            return aLong;
        }

        @Override
        public Long merge(Long aLong, Long acc1) {
            return aLong + acc1;
        }
    }

    // group by windowEnd field.
    public static class UserLogWindowKeySelector implements KeySelector<ZeusUserAnalysis, Long> {

        @Override
        public Long getKey(ZeusUserAnalysis value) throws Exception {
            return value.getWindowEnd();
        }
    }

    // select <userid,destHostIp> user key.
    public static class UserLogKeySelector
        implements KeySelector<ZeusFlinkUserAnalysisLog, Tuple3<String, String, String>> {

        @Override
        public Tuple3<String, String, String> getKey(ZeusFlinkUserAnalysisLog zeusFlinkUserAnalysisLog)
            throws Exception {
            return new Tuple3<>(zeusFlinkUserAnalysisLog.getUserId(), zeusFlinkUserAnalysisLog.getUserIpAddress(),
                zeusFlinkUserAnalysisLog.getDestHostIp());
        }
    }

    public static class UserLogDestHostIpKeySelector implements KeySelector<ZeusUserAnalysis, String> {

        @Override
        public String getKey(ZeusUserAnalysis value) throws Exception {
            return value.getDestHostIp();
        }
    }

    // assign timestamp.
    public static class ZeusLogTimestampAssigner implements SerializableTimestampAssigner<ZeusFlinkUserAnalysisLog> {

        @Override
        public long extractTimestamp(ZeusFlinkUserAnalysisLog zeusFlinkUserAnalysisLog, long l) {
            return zeusFlinkUserAnalysisLog.getTimestamp();
        }
    }

    // zeusLog -> zeusFlinkUserAnalysisLog
    public static class ZeusLogMapFunction implements MapFunction<ZeusLog, ZeusFlinkUserAnalysisLog> {
        @Override
        public ZeusFlinkUserAnalysisLog map(ZeusLog zeusLog) throws Exception {
            return new ZeusFlinkUserAnalysisLog(zeusLog.getId(), zeusLog.getUserId(), zeusLog.getSourceHostIp(),
                zeusLog.getDestHostIp(), zeusLog.getTime().getTime());
        }
    }
}
