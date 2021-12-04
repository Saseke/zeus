package com.songmengyuan.zeus.common.config.config;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Map;

@Data
public class Config {

    @SerializedName("server")
    private String server;

    @SerializedName("port_password")
    private Map<Integer, String> portPassword;

    @SerializedName("method")
    private String method;

    @SerializedName("obfs")
    private String obfs;

    @SerializedName("obfsparam")
    private String obfsParam;
    @SerializedName("token")
    private String token;
    @SerializedName("local_address")
    private String localAddress;

    @SerializedName("local_port")
    private Integer localPort;

}
