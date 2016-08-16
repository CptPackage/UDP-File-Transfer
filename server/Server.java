package me.filetransfer.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;

public class Server {

	private String fileSize;
	private byte[] incomingBytesContainer;
	private byte[] outcomingBytesContainer;
	private String fileExtension;
	private int serverPort;
	private DatagramSocket serverSocket;
	private DatagramPacket inPacket;
	private DatagramPacket outPacket;
	private JFileChooser targetFileChooser;
	private File targetFile;

	public Server(int serverPort) {
		try {
			this.serverPort = serverPort;
			targetFileChooser = new JFileChooser();
			serverSocket = new DatagramSocket(this.serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			System.out.println("Server started listening...");
			System.out.println("Waiting for clients.\n");
			System.out.println("Connected to client " + new String(connect()) + ".");
			targetFileChooser.showOpenDialog(null);
			sendFileSize();
			sendFileExtension(Paths.get(targetFileChooser.getSelectedFile().getPath()));
			sendData(Paths.get(targetFileChooser.getSelectedFile().getPath()));
			System.out.println("File is sent!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			cancelTransaction();
		}
	}

	private byte[] connect() throws IOException {
		incomingBytesContainer = new byte[256];
		inPacket = new DatagramPacket(incomingBytesContainer, incomingBytesContainer.length);
		serverSocket.receive(inPacket);
		return inPacket.getData();
	}

	private void sendFileSize() throws IOException {
		targetFile = new File(targetFileChooser.getSelectedFile().getAbsolutePath());
		fileSize = String.valueOf(targetFile.length());
		outcomingBytesContainer = new byte[256];
		outcomingBytesContainer = fileSize.getBytes();
		outPacket = new DatagramPacket(outcomingBytesContainer, outcomingBytesContainer.length, inPacket.getAddress(),
				inPacket.getPort());
		serverSocket.send(outPacket);
	}

	private void sendFileExtension(Path targetFilePath) throws IOException {
		outcomingBytesContainer = new byte[256];
		fileExtension = targetFileChooser.getSelectedFile().getAbsolutePath();
		fileExtension = fileExtension.substring(fileExtension.lastIndexOf("."), fileExtension.length());
		outcomingBytesContainer = fileExtension.getBytes();
		outPacket = new DatagramPacket(outcomingBytesContainer, outcomingBytesContainer.length, inPacket.getAddress(),
				inPacket.getPort());
		serverSocket.send(outPacket);
	}

	private void sendData(Path targetFilePath) throws IOException, InterruptedException {
		if (Integer.parseInt(fileSize.trim()) == 0) {
			cancelTransaction();
			return;
		}
		outcomingBytesContainer = new byte[Integer.parseInt(fileSize.trim())];
		outcomingBytesContainer = Files.readAllBytes(targetFilePath);
		outPacket = new DatagramPacket(outcomingBytesContainer, outcomingBytesContainer.length, inPacket.getAddress(),
				inPacket.getPort());
		serverSocket.send(outPacket);
	}

	private void cancelTransaction() {
		try {
			System.out.println("File transaction cancelled.");
			System.out.println("Connection with client closed.");
			outcomingBytesContainer = new byte[256];
			outcomingBytesContainer = new String("0").getBytes();
			outPacket = new DatagramPacket(outcomingBytesContainer, outcomingBytesContainer.length,
					inPacket.getAddress(), inPacket.getPort());
			serverSocket.send(outPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
