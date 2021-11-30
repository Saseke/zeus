package com.songmengyuan.zeus.log.analysis;

import com.songmengyuan.zeus.common.config.config.LogAnalysisConfig;
import com.songmengyuan.zeus.common.config.config.LogAnalysisConfigLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZeusLogAnalysisBootstrap {
    private static final ZeusLogAnalysisBootstrap zeusLogAnalysisBootstrap = new ZeusLogAnalysisBootstrap();

    public static ZeusLogAnalysisBootstrap getInstance() {
        return zeusLogAnalysisBootstrap;
    }

    public void start(String configPath) throws Exception {
        final LogAnalysisConfig config = LogAnalysisConfigLoader.load(configPath);
        log.info("load {} config file success", configPath);
        log.info("the config file information :{}", config);
    }
}
