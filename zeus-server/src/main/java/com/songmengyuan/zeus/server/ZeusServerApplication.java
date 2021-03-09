package com.songmengyuan.zeus.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class ZeusServerApplication {
    @Parameter(names = {"-c"}, help = true, description = "Specifies the location of the configuration file")
    private String path = Socks5ServerConstant.confPath;

    public static void main(String[] args) throws Exception {
        ZeusServerApplication application = new ZeusServerApplication();
        JCommander jct = JCommander.newBuilder()
                .addObject(application)
                .build();
        jct.setProgramName("zeus-server");
        try {
            jct.parse(args);
        } catch (ParameterException e) {
            jct.usage();
        }
        ZeusServerBootstrap.getInstance().start(application.path);
    }
}
