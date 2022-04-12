package com.songmengyuan.zeus.common.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZeusFlinkUserAnalysisLog {
    private String id;
    private String userId;
    private String userIpAddress;
    private String destHostIp;
    private Long timestamp;
}
