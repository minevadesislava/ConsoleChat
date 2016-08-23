package chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import chat.utils.Printer;
import chat.utils.Severity;

class ClientSender implements Runnable {	
	//------Client related------
	
	//We use print writer in order to send messages to client via socket
	private PrintWriter clientWriter;
	//Use queue for messages to clients, not sent them directly to printWriter
	private List<String> messages;
	
	//------Server related------
	private Socket socket;
	private Dispatcher dispatcher;
	
	public ClientSender(Client client, Dispatcher dispatcher) {
		this.socket = client.getSocket();
		this.dispatcher = dispatcher;
		messages = new ArrayList<String>();
		try {
			clientWriter = new PrintWriter(socket.getOutputStream());
		} catch (IOException ioEx) {
			this.dispatcher.removeClient(client.getName());
		}
	}
	
	@Override
	public void run() {
		while(dispatcher.isRunning() && dispatcher.getServerLoger().isAvailable()) {
			String message = getNextMessage();
			sendMessageToClient(message);
		}
	}
	
	synchronized void addMessageToClientsQueue(String message) {
		if(! (messages.size() == 0)) {
			try {
				wait();
			} catch (InterruptedException interruptedEx) {
				dispatcher.getServerLoger().log(Severity.FATAL, "server", "An error occured while adding message: " + Printer.getStackTrace(interruptedEx));
			}
		}
		messages.add(message);
		notify();
	}
	
	//if message queue is empty wais, otherwise get first message 
	synchronized String getNextMessage() {
		String result = null;
		if(messages.size() == 0 && dispatcher.isRunning()) {
			try {
				wait();
			} catch (InterruptedException interruptedEx) {
				dispatcher.getServerLoger().log(Severity.ERROR, "server", "Error while waiting for messages: " + Printer.getStackTrace(interruptedEx));
			}
		} else {
			result = messages.remove(0);	
		}
		return result;
	}
	
	//prints message in client writer
	private void sendMessageToClient(String message) {
		if(message != null) {
			clientWriter.println(message);
			clientWriter.flush();
		}
	}

	void closeResources() {
		clientWriter.close();		
	}
}