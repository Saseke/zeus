package com.songmengyuan.zeus.server;

public class ZeusServerApplication {

	public static void main(String[] args) throws Exception {
		ZeusServerBootstrap.getInstance().start("config-server-example.json");
	}

}
