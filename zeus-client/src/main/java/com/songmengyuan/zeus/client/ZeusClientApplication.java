package com.songmengyuan.zeus.client;

public class ZeusClientApplication {

	public static void main(String[] args) throws Exception {
		ZeusClientBootStrap.getInstance().start("config-client-example.json");
	}

}
