package com.songmengyuan.zeus.log.analysis;

import com.songmengyuan.zeus.common.config.config.LogAnalysisConfig;
import com.songmengyuan.zeus.common.config.config.LogAnalysisConfigLoader;
import com.songmengyuan.zeus.log.analysis.constant.LogAnalysisConstant;
import com.songmengyuan.zeus.log.analysis.flink.boot.ZeusFlinkBootstrap;
import com.songmengyuan.zeus.log.analysis.storm.server.boot.ZeusStormBootstrap;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class ZeusLogAnalysisApplication {
    public static void main(String[] args) throws Exception {
        final LogAnalysisConfig config = LogAnalysisConfigLoader.load(LogAnalysisConstant.configPath);
        log.info("load {} config file success", LogAnalysisConstant.configPath);
        log.info("the config file information :{}", config);
        if (config.getCalculateMethod().equals(LogAnalysisConstant.STORM_CALCULATE_METHOD)) {
            ZeusStormBootstrap.start(config, args);
        } else {
            ZeusFlinkBootstrap.start(config);
        }
    }
}
