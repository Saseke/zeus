package com.songmengyuan.zeus.common.config.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZeusUserAnalysisLog {
    private String id;
    private String userId;
    private String userIpAddress;
    private String url;
    private Date date;
}
