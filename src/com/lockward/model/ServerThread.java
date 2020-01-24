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

	public ServerThread(Socket client, ChatServer chatServer) {
		this.client = client;
		this.chatServer = chatServer;
	}

	@Override
	public void run() {
		Message message = null;
		try {
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

				// output.writeObject(new Message(MessageType.OKAY, "Received",
				// "Server"));
				// output.flush();
			}

		} catch (IOException e) {
			System.out.println("Server Error: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				System.out.println("Error trying to close client connection: " + e.getMessage());
			}
		}
	}

	public Socket getClient() {
		return client;
	}

	private void parseInput(Socket client, Message message) {
		System.out.println("Parsing input: " + message.getMessage());

		switch (message.getMessageType()) {
		case REGISTER:
			chatServer.registerNewUser(client, message.getUsername());
			break;
		case LOGOFF:
			System.out.println(message.getMessage());
			try {
				client.close();

				if (chatServer.removeClient(client)) {
					System.out.println(message.getUsername() + " disconnected");
				}
			} catch (IOException e) {
				System.err.println("Error cerrando conexión para usuario: " + message.getUsername());
				System.err.println("Error: " + e.getMessage());
			}

			break;
		case TEXT:
//			try {
//				output.writeObject(message);
//				output.flush();
//			} catch (IOException e) {
//				System.out.println("Error forwarding message: " + e.getMessage());
//			}
			chatServer.broadcast(message);
		default:
			break;
		}
	}

	public void forwardMessage(Message message) throws IOException {
		output.writeObject(message);
		output.flush();
	}

}
