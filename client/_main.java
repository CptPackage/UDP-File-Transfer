package me.filetransfer.client;

public class _main {

	public static void main(String[] args) {
		Client clientFileTransfer = new Client("192.168.1.4", 9111);
		clientFileTransfer.start();
	}
}