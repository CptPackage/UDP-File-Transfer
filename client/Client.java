package me.filetransfer.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;

public class Client {

	private String serverIP = "127.0.0.1";
	private int serverPort;
	private String message;
	private String fileExtension;
	private int fileSize;
	private DatagramSocket clientSocket;
	private DatagramPacket inPacket;
	private DatagramPacket outPacket;
	private Path targetFilePath;
	private FileOutputStream fileHandler;
	private InetAddress IPAddress;
	private JFileChooser targetFolderChooser;
	private byte[] incomingBytesContainer;
	private byte[] outcomingBytesContainer;

	public Client(String serverIP, int serverPort) {
		this.serverIP = serverIP;
		this.serverPort = serverPort;
		try {
			targetFolderChooser = new JFileChooser();
			clientSocket = new DatagramSocket();
			IPAddress = InetAddress.getLocalHost();
			message = "(" + IPAddress.getHostAddress() + ":" + serverPort + ")";
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	public void start() {
		try {
			System.out.println("Connecting to the server IP (" + serverIP + ":" + serverPort + ").");
			connect();
			System.out.println("Receiving file...");
			fileSize = Integer.parseInt(new String(receiveFileAttributes()).trim());
			if (fileSize == 0) {
				System.out.println("Transaction cancelled.");
				System.out.println("Server closed connection.");
				return;
			}
			fileExtension = new String(receiveFileAttributes());
			receiveData();
			saveFile();
			System.out.println("File is received.");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println("File not saved.");
		}
	}

	private void connect() throws IOException {
		outcomingBytesContainer = new byte[message.getBytes().length];
		outcomingBytesContainer = message.getBytes();
		outPacket = new DatagramPacket(outcomingBytesContainer, outcomingBytesContainer.length,
				InetAddress.getByName(serverIP), serverPort);
		clientSocket.send(outPacket);
	}

	private byte[] receiveFileAttributes() throws IOException {
		incomingBytesContainer = new byte[256];
		inPacket = new DatagramPacket(incomingBytesContainer, incomingBytesContainer.length);
		clientSocket.receive(inPacket);
		incomingBytesContainer = inPacket.getData();
		return incomingBytesContainer;
	}

	private void receiveData() throws IOException {
		incomingBytesContainer = new byte[fileSize];
		inPacket = new DatagramPacket(incomingBytesContainer, incomingBytesContainer.length);
		clientSocket.receive(inPacket);
		incomingBytesContainer = inPacket.getData();
	}

	private void saveFile() throws IOException, NullPointerException {
		targetFolderChooser.showSaveDialog(null);
		targetFilePath = Paths.get((targetFolderChooser.getCurrentDirectory().getAbsolutePath() + "\\"
				+ targetFolderChooser.getSelectedFile().getName() + fileExtension).trim());
		Files.createFile(targetFilePath);
		fileHandler = new FileOutputStream(targetFilePath.toFile());
		fileHandler.write(incomingBytesContainer);
	}
}