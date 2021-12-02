package com.songmengyuan.zeus.log.analysis.server.boot;

import com.songmengyuan.zeus.log.analysis.constant.LogAnalysisConstant;

public class ZeusLogAnalysisApplication {
    public static void main(String[] args) throws Exception {
        ZeusLogAnalysisBootstrap.start(LogAnalysisConstant.configPath,args);
    }
}
