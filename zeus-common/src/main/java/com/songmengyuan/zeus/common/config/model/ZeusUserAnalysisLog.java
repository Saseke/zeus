package com.songmengyuan.zeus.common.config.model;

import lombok.Data;

import java.util.Set;

@Data
public class ZeusUserAnalysisLog {
    private String id;
    private String userIpAddress;
    private Set<String> histories;
}
