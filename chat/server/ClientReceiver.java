package chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;

import chat.utils.Constants;
import chat.utils.Printer;
import chat.utils.Severity;

class ClientReceiver implements Runnable {
	//------Client related------
	private Client client;
	
	//------Server related------
	private Dispatcher dispatcher;
	private Socket clientSocket;
	//BufferedReader is used in order to read lines
	private BufferedReader clientReader;
	
	
	ClientReceiver(Client client, Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
		this.client = client;
		this.clientSocket = client.getSocket();
		try {
			clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException ioEx) {
			dispatcher.getServerLoger().log(Severity.FATAL, "server", "An error occurred while openning socket reader: " + Printer.getStackTrace(ioEx));
			this.dispatcher.removeClient(client.getName());
		}
	}
	
	public void closeResources() throws IOException {
		clientReader.close();
	}
	
	//reads lines from buffered reader while dispatcher is running
	public void run() {			
		while(dispatcher.isRunning() && dispatcher.getServerLoger().isAvailable()) {
			String message;
			try {
				message = clientReader.readLine();
				//after reading line unpack message
				if(message.contains(Constants.NICKNAME_COMMAND)){
					changeClientName(message);
				} else if(message.contains(Constants.QUIT_COMMAND)) {
					quit();
				} else {
					dispatcher.addMessageToQueue(client, message);
				}	
			} catch (IOException ioEx) {
				dispatcher.getServerLoger().log(Severity.FATAL,"server", "An error occurred while reading from socket: " + Printer.getStackTrace(ioEx));
				dispatcher.removeClient(client.getName());
				break;
			}
						
		}
		return;
	}
	
	//remove client from client's map and close connection
	private void quit() {
		dispatcher.removeClient(this.client.getName());
		dispatcher.getServerLoger().log(Severity.DEBUG, "server", "client " + this.client.getName() + "| IP: " + this.client.getIP() + " has been disconnected at " + LocalDateTime.now());
		System.out.println("client " + this.client.getName() + "| IP: " + this.client.getIP() + " has been disconnected at " + LocalDateTime.now());
	}
	
	//check if nickname is already used or is less than 3 symbols and add new key-value for this client
	private void changeClientName(String line) {
		String nickname = line.replace(Constants.NICKNAME_COMMAND, "");
		if (nickname.length() < 3) {
			client.getSender().addMessageToClientsQueue("Username must be at least 3 symbols. Please try again :)");
			return;
		}
		//if client's map doesn't contain this nickname, set clients with new name and notify other clients for this change
		if(!dispatcher.getClients().containsKey(nickname)) {
			String oldName = client.getName();
			dispatcher.getClients().put(nickname, dispatcher.getClients().remove(client.getName()));
			client.setName(nickname);
			dispatcher.addMessageToQueue(client, oldName + " changes nickname to: " + nickname);
			System.out.println("client " + oldName + "| IP: " + this.client.getIP() + " changes nickname to:  " + nickname + " at " + LocalDateTime.now());
		} else {
			client.getSender().addMessageToClientsQueue("There is already user with this name. Please try again :)" );
		}
	}
}