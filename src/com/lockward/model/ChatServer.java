package com.lockward.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer extends Thread {
	List<Socket> clients = new ArrayList<>();
	List<ChatClient> clientList = new ArrayList<>();
	List<ServerThread> activeConnections = new ArrayList<>();

	private ServerSocket serverSocket;
	private final ExecutorService connectionPool = Executors.newFixedThreadPool(20);
	private Socket client = null;

	public void connect() throws IOException {
		if (serverSocket == null || isClosed()) {
			serverSocket = new ServerSocket(5000);
		}
	}

	public void close() throws IOException {
		connectionPool.shutdown();

		try {
			if (!connectionPool.awaitTermination(5, TimeUnit.SECONDS)) {
				connectionPool.shutdownNow();

				if (!connectionPool.awaitTermination(5, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException e) {
			connectionPool.shutdown();
			Thread.currentThread().interrupt();
		}

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
				client = serverSocket.accept();

				System.out.println("Client connected");
				System.out.println("Client IP: " + client.getInetAddress());
				System.out.println("Client port: " + client.getPort());

				ServerThread newConnection = new ServerThread(client, this);
				activeConnections.add(newConnection);

				connectionPool.execute(newConnection);
			}

			connectionPool.shutdown();
		} catch (IOException e) {
			System.out.println("Server Error: " + e.getMessage());
			connectionPool.shutdown();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Error Closing Server Connection: " + e.getMessage());
			}
		}

	}

	void registerNewUser(Socket client, String username) {
		System.out.println("Registrando usuario: " + username);
		clients.add(client);
		System.out.println("Clients: " + clients.size());
		// clientList.add(new ChatClient(username, client));
	}

	void broadcast(Message message) {
		for(ServerThread activeConnection : activeConnections) {
			try {
				activeConnection.forwardMessage(message);
			} catch (IOException e) {
				System.err.println("Error forwarding message: " + e.getMessage());
			}
		}
	}

	public void process() {
		// for(ChatClient c : clientList) {
		//
		// }
	}

	public boolean removeClient(Socket client) {
		ServerThread clientConnection = null;

		for(ServerThread conn : activeConnections) {
			if(conn.getClient() == client) {
				clientConnection = conn;
				break;
			}
		}

		if(clientConnection != null) {
			activeConnections.remove(clientConnection);
			System.out.println("Connection removed successfully");
		}

		return clients.remove(client);
	}
}
