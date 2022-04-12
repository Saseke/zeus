package com.songmengyuan.zeus.common.config.model;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

import com.songmengyuan.zeus.common.config.constant.ZeusLogLevel;
import com.songmengyuan.zeus.common.config.util.GsonUtil;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZeusLog implements Serializable {
    private String id;
    private ZeusLogLevel level;
    private Date time;
    private String userId;
    private String sourceHostIp;
    private String sourceHostPort;
    private String sourceHostName;
    private String clientChannelId;
    private String destHostIp;
    private String destHostPort;
    private String destHostName;
    private String message;
    private Integer traffic;

    public static ZeusLog createSystemLog(String message, Date time) {
        ZeusLog log = new ZeusLog();
        log.id = UUID.randomUUID().toString();
        log.level = ZeusLogLevel.SYSTEM;
        log.time = time;
        log.message = message;
        return log;
    }

    public static ZeusLog createRecordLog(Date time, String sourceHostIp, String sourceHostPort, String sourceHostName,
        String clientChannelId, String destHostIp, String destHostPort, String destHostName, String message,
        String userId) {
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
        log.userId = userId;
        return log;
    }

    public static ZeusLog createTrafficLog(Date time, String sourceHostIp, String sourceHostPort, String sourceHostName,
        String destHostIp, String destHostPort, String destHostName, String message, String userId, Integer traffic) {
        ZeusLog log = new ZeusLog();
        log.id = UUID.randomUUID().toString();
        log.level = ZeusLogLevel.TRAFFIC;
        log.time = time;
        log.sourceHostIp = sourceHostIp;
        log.sourceHostPort = sourceHostPort;
        log.sourceHostName = sourceHostName;
        log.destHostIp = destHostIp;
        log.destHostPort = destHostPort;
        log.destHostName = destHostName;
        log.message = message;
        log.userId = userId;
        log.traffic = traffic;
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

    public static void recordTrafficLog(InetSocketAddress sourceAddress, InetSocketAddress destAddress, String token,
        int traffic) {
        String trafficMsg = String.format("token: %s traffic %s byte .", token, traffic);
        ZeusLog trafficLog = ZeusLog.createTrafficLog(new Date(), sourceAddress.getAddress().getHostAddress(),
            String.valueOf(sourceAddress.getPort()), sourceAddress.getHostName(),
            destAddress.getAddress().getHostAddress(), String.valueOf(destAddress.getPort()), destAddress.getHostName(),
            trafficMsg, token, traffic);
        log.info(GsonUtil.getGson().toJson(trafficLog));
    }

    public ZeusLog(String id, ZeusLogLevel level, Date time, String message) {
        this.id = id;
        this.level = level;
        this.time = time;
        this.message = message;
    }
}
