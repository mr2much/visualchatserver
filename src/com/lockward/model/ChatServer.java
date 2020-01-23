package com.lockward.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer extends Thread {
	List<ChatClient> clientList = new ArrayList<>();

	private ServerSocket serverSocket;

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
		System.out.println("Server up and running...");
		try {
			while (!serverSocket.isClosed()) {
				Socket client;

				client = serverSocket.accept();

				System.out.println("Client connected");
				System.out.println("Client IP: " + client.getInetAddress());
				System.out.println("Client port: " + client.getPort());

				new ServerThread(client, this).start();
			}
		} catch (IOException e) {
			System.out.println("Server Error: " + e.getMessage());
			Thread.currentThread().interrupt();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Error Closing Server Connection: " + e.getMessage());
			}
		}

	}

	void registerNewUser(Socket client, String username) throws IOException {
		System.out.println("Registrando usuario: " + username);
//		clientList.add(new ChatClient(username, client));
	}

	void broadcast(Message message) {
		System.out.println("Message received: " + message.getMessage());
	}

	public void process() {
		// for(ChatClient c : clientList) {
		//
		// }
	}
}
