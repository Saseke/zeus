package com.songmengyuan.zeus.client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.songmengyuan.zeus.client.socks5.Socks5Constant;

public class ZeusClientApplication {

    @Parameter(names = {"-c"}, help = true, description = "Specifies the location of the configuration file")
    private String path = Socks5Constant.confPath;

    public static void main(String[] args) throws Exception {
        ZeusClientApplication application = new ZeusClientApplication();
        JCommander jct = JCommander.newBuilder()
                .addObject(application)
                .build();
        jct.setProgramName("zeus");
        try {
            jct.parse(args);
        } catch (ParameterException e) {
            jct.usage();
        }
        ZeusClientBootStrap.getInstance().start(application.path);
    }

}
