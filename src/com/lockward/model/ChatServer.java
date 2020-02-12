package com.lockward.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChatServer extends Thread {
	List<ObjectOutputStream> broadcastList = new ArrayList<>();
	Map<String, ObjectOutputStream> listeners = new HashMap<>();
	private ConcurrentSkipListSet<String> users = new ConcurrentSkipListSet<>();

	private ServerSocket serverSocket;
	private final ExecutorService connectionPool = Executors.newFixedThreadPool(20);
	private Socket client = null;

	private MessageBuilder builder = new MessageBuilder();

	public void connect() throws IOException {
		if (serverSocket == null || isClosed()) {
			serverSocket = new ServerSocket(5000);
		}
	}

	public void close() throws IOException {
		closeClientConnections();
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

	private void closeClientConnections() throws IOException {
		// notify all users that the server is being shutdown
		broadcast(builder.messageType(MessageType.STATUS).subMessageType(MessageType.SHUTDOWN)
				.msg("Last words are for fools who haven't said enough!").username("Server").build());

		// iterate through all client connections and close them
		for (String username : listeners.keySet()) {
			ObjectOutputStream oos = listeners.get(username);
			oos.close();
		}
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
				System.out.println("adding new connection");
				System.out.println("running new thread");
				connectionPool.execute(newConnection);
			}

			connectionPool.shutdown();
		} catch (IOException e) {
			if (e.getMessage().equalsIgnoreCase("socket closed")) {
				System.out.println("Fuck off");
			} else {
				System.out.println("Server Error: " + e.getMessage());
			}

			connectionPool.shutdown();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("Error Closing Server Connection: " + e.getMessage());
			}
		}

	}

	void registerNewUser(Socket client, ObjectOutputStream oos, String username) {
		System.out.println("Registrando usuario: " + username);

		// announce new client to other users

		// create a direct output stream to the client, and add it to a list for
		// broadcast
		listeners.put(username, oos);
		broadcastList.add(oos);
		users.add(username);

		broadcast(builder.messageType(MessageType.STATUS).subMessageType(MessageType.ADD)
				.msg(username + " has logged in").attachment(users.toArray()).username("Server").build());

	}

	void broadcast(Message message) {
		System.out.println("Broadcasting message..." + message.getMessage() + " from: " + message.getUsername());
		System.out.println("To: " + listeners.size() + " clients");

		for (String username : listeners.keySet()) {
			try {
				ObjectOutputStream out = listeners.get(username);
				out.writeObject(message);
				out.flush();
			} catch (IOException e) {
				System.out.println("Error sending message: " + e.getMessage());
			}

		}

	}

	public void process() {
		// for(ChatClient c : clientList) {
		//
		// }
	}

	public boolean removeClient(String username, ObjectOutputStream oos) {
		if (listeners.containsKey(username)) {
			users.remove(username);
			return listeners.remove(username, oos);
		}

		return false;
	}
}
