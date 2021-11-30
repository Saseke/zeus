package com.songmengyuan.zeus.log.analysis;

import com.songmengyuan.zeus.log.analysis.constant.LogAnalysisConstant;

public class ZeusLogAnalysisApplication {
    public static void main(String[] args) throws Exception {
        ZeusLogAnalysisBootstrap.getInstance().start(LogAnalysisConstant.configPath);
    }
}
