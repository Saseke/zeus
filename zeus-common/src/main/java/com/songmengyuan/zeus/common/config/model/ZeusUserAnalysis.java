package com.songmengyuan.zeus.common.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZeusUserAnalysis {
    private String userId;
    private String userIpAddress;
    private Long windowEnd;
    private String destHostIp;
    private Long count;
}
