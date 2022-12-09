package net.codejava.networking.chat.server;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This thread handles connection for each connected client, so the server
 * can handle multiple clients at the same time.
 *
 * @author www.codejava.net
 */
public class UserThread extends Thread {
	private Socket socket;
	private ChatServer server;
	private PrintWriter writer;
	private HashMap<UserThread, String> users = new HashMap<>();

	public UserThread(Socket socket, ChatServer server) {
		this.socket = socket;
		this.server = server;
	}

	public void run() {
		try {
			InputStream input = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

			OutputStream output = socket.getOutputStream();
			writer = new PrintWriter(output, true);

			printUsers();

			String userName = reader.readLine();
			server.addUserName(userName);
			users = server.getUsers();

			String serverMessage = "New user connected: " + userName;
			server.broadcast(serverMessage, this);
			users.replace(this, userName);

			String clientMessage;

			do {
                clientMessage = reader.readLine();
                String[] messageParts = clientMessage.split(" ");
                serverMessage = "[" + userName + "]: " + clientMessage;
                if (clientMessage.charAt(0) == '@') {
                	
                	//if nothing after @, throws an exception
                	for (UserThread name: server.getUserThreads()) {
                    	System.out.println("checking users");
                    		//messageParts[0]
                		if (messageParts[0].contains(users.get(name))) {
                			server.privMessage(name, "(Private)" + serverMessage, this);
                		}
                		else {
                			System.out.println("No valid users specified");
                		}
                	}
                	
                }
                else {
                    server.broadcast(serverMessage, this);
                }
 
            } while (!clientMessage.equals("bye"));

			server.removeUser(userName, this);
			socket.close();

			serverMessage = userName + " has quitted.";
			server.broadcast(serverMessage, this);

		} catch (IOException ex) {
			System.out.println("Error in UserThread: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Sends a list of online users to the newly connected user.
	 */
	void printUsers() {
		if (server.hasUsers()) {
			writer.println("Connected users: " + server.getUserNames());
		} else {
			writer.println("No other users connected");
		}
	}

	/**
	 * Sends a message to the client.
	 */
	void sendMessage(String message) {
		writer.println(message);
	}
}