package com.lockward.model;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer extends Thread {
	// List<Socket> clientList = new ArrayList<>();
	List<ChatClient> clientList = new ArrayList<>();

	private ServerSocket serverSocket;
	private Echoer echoer;

	public void connect() throws IOException {
		if (serverSocket == null || isClosed()) {
			serverSocket = new ServerSocket(5000);
		}
	}

	public void close() throws IOException {
		serverSocket.close();
		this.interrupt();
	}

	public boolean isClosed() {
		return serverSocket.isClosed();
	}

	@Override
	public void run() {
		try {
			while (!serverSocket.isClosed()) {
				Socket client;

				client = serverSocket.accept();

				System.out.println("Client connected");
				ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
				output.flush();
				ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
//				PrintWriter output = new PrintWriter(client.getOutputStream());
				// ChatClient chatClient = new ChatClient(client);
				// clientList.add(chatClient);

				Message message = (Message) input.readObject();
				parseInput(message);
//				echoer = new Echoer(client);
//				echoer.start();
			}
		} catch (IOException e) {
			System.out.println("Server Error: " + e.getMessage());
			Thread.currentThread().interrupt();
			echoer = null;
		} catch (ClassNotFoundException e) {
			System.out.println("Error Parsing Message: " + e.getMessage());
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Error Closing Server Connection: " + e.getMessage());
			}
		}

	}

	private void parseInput(Message message) {
		System.out.println("Parsing input: " + message.getMessage());
		switch (message.getMessageType()) {
		case REGISTER:
			registerNewUser(message.getUsername());
			break;
		default:
			break;
		}
	}

	private void registerNewUser(String username) {
		System.out.println("Registrando usuario: " + username);
	}

	public void process() {
		// for(ChatClient c : clientList) {
		//
		// }
	}
}
