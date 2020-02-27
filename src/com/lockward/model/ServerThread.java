package com.lockward.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {

	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket client = null;
	private ChatServer chatServer;
	private MessageBuilder builder = new MessageBuilder();

	public ServerThread(Socket client, ChatServer chatServer) {
		this.client = client;
		this.chatServer = chatServer;
		if (client == null) {
			System.out.println("Nothing to see here");
		}

	}

	@Override
	public void run() {
		try {
			Message message = null;

			if (output == null) {
				output = new ObjectOutputStream(client.getOutputStream());
			}

			if (input == null) {
				input = new ObjectInputStream(client.getInputStream());
			}

			// keep the server communicating with the client
			while ((message = (Message) input.readObject()) != null) {
				if (message != null) {
					System.out.println("Message: " + message.getMessage());
					parseInput(client, message);
				}
			}

		} catch (IOException e) {
			if (e.getMessage().equalsIgnoreCase("socket closed")) {

			} else {
				System.out.println("Server Error: " + e.getMessage());
			}

		} catch (ClassNotFoundException e) {
			System.out.println("Server error processing the message: " + e.getMessage());
		} finally {
			try {
				output.close();
				input.close();
				client.close();
				this.interrupt();
			} catch (IOException e) {
				System.out.println("Error trying to close client connection: " + e.getMessage());
			}
		}
	}

	public Socket getClient() {
		return client;
	}

	public ObjectOutputStream getObjectOutputStream() {
		return output;
	}

	private void parseInput(Socket client, Message message) {
		System.out.println("Parsing input: " + message.getMessage());

		String username = message.getUsername();

		switch (message.getMessageType()) {
		case REGISTER:
			chatServer.registerNewUser(client, output, username);
			break;
		case LOGOFF:
			System.out.println(message.getMessage());
			try {
				if (chatServer.removeClient(username, output)) {
					chatServer.broadcast(builder.messageType(MessageType.STATUS).subMessageType(MessageType.LOGOFF)
							.msg(message.getMessage()).username("Server").build());
					System.out.println(username + " disconnected");
				} else {
					System.out.println("Client not removed: " + client.getInetAddress());
				}
				this.interrupt();
				output.close();
				input.close();
				client.close();
			} catch (IOException e) {
				System.err.println("Error cerrando conexión para usuario: " + username);
				System.err.println("Error: " + e.getMessage());
			}

			break;
		case TEXT:
			message.setSubMessageType(MessageType.TEXT);
			chatServer.broadcast(message);
			break;
		default:
			break;
		}
	}
}
