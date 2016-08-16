package me.filetransfer.server;

public class _main {
	public static void main(String[] args) {
		Server fileTransferServer = new Server(9111);
		fileTransferServer.start();
	}
}
