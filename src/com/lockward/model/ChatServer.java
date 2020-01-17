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

	private ObjectOutputStream output;
	private ObjectInputStream input;
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
				System.out.println("Client IP: " + client.getInetAddress());
				System.out.println("Client port: " + client.getPort());

				if (output == null) {
					output = new ObjectOutputStream(client.getOutputStream());
				}

				if (input == null) {
					input = new ObjectInputStream(client.getInputStream());
				}

				// PrintWriter output = new
				// PrintWriter(client.getOutputStream());
				// ChatClient chatClient = new ChatClient(client);
				// clientList.add(chatClient);

				Message message = (Message) input.readObject();

				if (message != null) {
					System.out.println("Message: " + message.getMessage());
				}

				parseInput(client, message);

				output.writeObject(new Message(MessageType.OKAY, "Received", "Server"));
				output.flush();

				// echoer = new Echoer(client);
				// echoer.start();
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

	private void parseInput(Socket client, Message message) throws IOException {
		System.out.println("Parsing input: " + message.getMessage());
		switch (message.getMessageType()) {
		case REGISTER:
			registerNewUser(client, message.getUsername());
			break;
		case TEXT:
			broadcast(message);
		default:
			break;
		}
	}

	private void registerNewUser(Socket client, String username) throws IOException {
		System.out.println("Registrando usuario: " + username);
		clientList.add(new ChatClient(username, client));
	}

	private void broadcast(Message message) {
		System.out.println("Message received: " + message.getMessage());
	}

	public void process() {
		// for(ChatClient c : clientList) {
		//
		// }
	}
}
