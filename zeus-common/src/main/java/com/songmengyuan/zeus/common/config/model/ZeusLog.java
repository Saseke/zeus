package com.songmengyuan.zeus.common.config.model;

import java.util.Date;
import java.util.UUID;

import com.songmengyuan.zeus.common.config.constant.ZeusLogLevel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ZeusLog {
    private String id;
    private ZeusLogLevel level;
    private Date time;
    private String sourceHostIp;
    private String sourceHostPort;
    private String sourceHostName;
    private String clientChannelId;
    private String destHostIp;
    private String destHostPort;
    private String destHostName;
    private String message;

    public static ZeusLog createSystemLog(String message, Date time) {
        ZeusLog log = new ZeusLog();
        log.id = UUID.randomUUID().toString();
        log.level = ZeusLogLevel.SYSTEM;
        log.time = time;
        log.message = message;
        return log;
    }

    public static ZeusLog createRecordLog(Date time, String sourceHostIp, String sourceHostPort, String sourceHostName,
        String clientChannelId, String destHostIp, String destHostPort, String destHostName, String message) {
        ZeusLog log = new ZeusLog();
        log.id = UUID.randomUUID().toString();
        log.level = ZeusLogLevel.RECORD;
        log.time = time;
        log.sourceHostIp = sourceHostIp;
        log.sourceHostPort = sourceHostPort;
        log.sourceHostName = sourceHostName;
        log.clientChannelId = clientChannelId;
        log.destHostIp = destHostIp;
        log.destHostPort = destHostPort;
        log.destHostName = destHostName;
        log.message = message;
        return log;
    }

    public static ZeusLog createErrorLog(String message, Date time) {
        ZeusLog log = new ZeusLog();
        log.id = UUID.randomUUID().toString();
        log.level = ZeusLogLevel.ERROR;
        log.time = time;
        log.message = message;
        return log;
    }
}
