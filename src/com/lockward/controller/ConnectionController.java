package com.lockward.controller;

import java.io.IOException;

import com.lockward.model.ChatServer;

public class ConnectionController {
	private ChatServer chatServer;

	public ConnectionController() {
		chatServer = new ChatServer();
	}

	public void connect() throws IOException {
		if (chatServer == null) {
			chatServer = new ChatServer();
		}

		chatServer.connect();
	}

	public void close() throws IOException {
		if (chatServer != null) {
			chatServer.close();
			chatServer = null;
		}

	}

	public boolean isClosed() {
		return (chatServer == null) ? true : chatServer.isClosed();
	}

	public void process() {
		chatServer.start();
	}
}
